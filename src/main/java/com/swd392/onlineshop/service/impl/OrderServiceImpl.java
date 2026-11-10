package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.config.AppProperties;
import com.swd392.onlineshop.dto.request.AssignOrderForm;
import com.swd392.onlineshop.dto.request.OrderFilter;
import com.swd392.onlineshop.dto.request.OrderUpdateForm;
import com.swd392.onlineshop.dto.request.ReceiverForm;
import com.swd392.onlineshop.dto.response.CartLine;
import com.swd392.onlineshop.dto.response.OrderHistoryView;
import com.swd392.onlineshop.dto.response.OrderLineView;
import com.swd392.onlineshop.dto.response.OrderRow;
import com.swd392.onlineshop.dto.response.OrderView;
import com.swd392.onlineshop.dto.response.SaleOption;
import com.swd392.onlineshop.entity.Customer;
import com.swd392.onlineshop.entity.Order;
import com.swd392.onlineshop.entity.OrderHistory;
import com.swd392.onlineshop.entity.OrderItem;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.enums.OrderStatus;
import com.swd392.onlineshop.enums.UserStatus;
import com.swd392.onlineshop.exception.BusinessException;
import com.swd392.onlineshop.exception.ForbiddenException;
import com.swd392.onlineshop.exception.NotFoundException;
import com.swd392.onlineshop.exception.OutOfStockException;
import com.swd392.onlineshop.repository.OrderItemRepository;
import com.swd392.onlineshop.repository.OrderRepository;
import com.swd392.onlineshop.repository.OrderHistoryRepository;
import com.swd392.onlineshop.repository.ProductRepository;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.CartService;
import com.swd392.onlineshop.service.CustomerService;
import com.swd392.onlineshop.service.EmailService;
import com.swd392.onlineshop.service.OrderService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    public static final String SESSION_EDITING_ORDER_ID = "EDITING_ORDER_ID";
    public static final String SESSION_LAST_ORDER_ID = "LAST_ORDER_ID";

    private static final String SALE = "SALE";
    private static final String SALE_MANAGER = "SALE_MANAGER";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final CartService cartService;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Value("${app.page-size:10}")
    private int pageSize;

    // ==========================================
    // SALE & SALE MANAGER METHODS
    // ==========================================

    @Override
    public Page<OrderRow> list(OrderFilter filter) {
        User viewer = currentUser();
        int pageIndex = filter.getPage() == null || filter.getPage() < 1 ? 0 : filter.getPage() - 1;
        Page<Order> orders = orderRepository.findAll(
                specification(filter, viewer),
                PageRequest.of(pageIndex, pageSize, sort(filter)));
        Map<Long, List<String>> productNames = productNames(orders.getContent());
        return orders.map(order -> toDto(order, productNames.getOrDefault(order.getId(), List.of())));
    }

    @Override
    public OrderView getDetail(Long id) {
        Order order = requireOrder(id);
        requireCanHandle(order, currentUser());
        return toDto(order, orderHistoryRepository.findByOrderIdOrderByCreatedAtAsc(id));
    }

    @Override
    @Transactional
    public void update(Long id, OrderUpdateForm form) {
        Order order = requireOrder(id);
        requireCanHandle(order, currentUser());

        OrderStatus before = order.getStatus();
        String noteBefore = order.getSaleNote();
        String note = blankToNull(form.getSaleNote());

        changeStatus(order, form.getStatus());
        order.setSaleNote(note);

        if (before != order.getStatus() || !Objects.equals(noteBefore, note)) {
            writeHistory(order, updateContent(before, order.getStatus(), !Objects.equals(noteBefore, note)));
        }
    }

    @Override
    @Transactional
    public void assign(Long id, AssignOrderForm form) {
        User viewer = currentUser();
        if (!isManager(viewer)) {
            throw new ForbiddenException("Chỉ Sale Manager được giao đơn.");
        }
        Order order = requireOrder(id);
        User sale = requireActiveSale(form.getSaleId());
        if (order.getAssignedSale() != null && sale.getId().equals(order.getAssignedSale().getId())) {
            return;
        }
        order.setAssignedSale(sale);
        writeHistory(order, "Giao đơn cho " + sale.getFullName() + ".");
    }

    @Override
    public List<SaleOption> activeSales() {
        return userRepository.findByRoleValueAndStatus(UserStatus.ACTIVE).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public boolean viewingAll() {
        return isManager(currentUser());
    }

    // ==========================================
    // CUSTOMER & PUBLIC CHECKOUT METHODS
    // ==========================================

    @Override
    @Transactional
    public Order placeOrder(ReceiverForm form, HttpSession session, Long userId) {
        List<CartLine> cartLines = cartService.getCartLines(session, userId);
        if (cartLines.isEmpty()) {
            throw new BusinessException("Giỏ hàng của bạn đang trống.");
        }

        Object editingOrderIdObj = session != null ? session.getAttribute(SESSION_EDITING_ORDER_ID) : null;
        if (editingOrderIdObj != null) {
            Long editingOrderId = Long.valueOf(editingOrderIdObj.toString());
            return handleUpdateExistingOrder(editingOrderId, form, cartLines, session, userId);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Product> lockedProducts = new ArrayList<>();

        for (CartLine line : cartLines) {
            Product product = productRepository.findByIdWithLock(line.getProductId())
                    .orElseThrow(() -> new OutOfStockException(line.getName()));

            if (!product.isVisible() || product.getStockQuantity() < line.getQuantity()) {
                throw new OutOfStockException(product.getName());
            }
            lockedProducts.add(product);
            totalAmount = totalAmount.add(product.getCurrentPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }

        Customer receiverCustomer = customerService.upsertFromReceiver(form);

        Customer orderCustomer;
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng."));
            orderCustomer = customerService.findByUserId(userId).orElseGet(() -> {
                Optional<Customer> matchOpt = customerService.findByEmail(user.getEmail());
                if (matchOpt.isPresent()) {
                    Customer c = matchOpt.get();
                    c.setUser(user);
                    return c;
                }
                Customer newC = customerService.findOrCreateContact(user.getEmail(), user.getFullName(), user.getGender(), user.getMobile());
                newC.setUser(user);
                return newC;
            });
        } else {
            orderCustomer = receiverCustomer;
        }

        Order order = new Order();
        order.setCode(generateOrderCode());
        order.setCustomer(orderCustomer);
        order.setReceiverName(form.getFullName().trim());
        order.setReceiverGender(form.getGender());
        order.setReceiverEmail(form.getEmail().trim().toLowerCase());
        order.setReceiverMobile(form.getMobile().trim());
        order.setReceiverAddress(form.getAddress().trim());
        order.setNote(form.getNote() != null ? form.getNote().trim() : null);
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(form.getPaymentMethod());
        order.setStatus(OrderStatus.SUBMITTED);

        assignRoundRobinSale(order);

        Order savedOrder = orderRepository.save(order);

        for (int i = 0; i < cartLines.size(); i++) {
            CartLine line = cartLines.get(i);
            Product p = lockedProducts.get(i);

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(p);
            item.setProductCode(p.getCode());
            item.setProductName(p.getName());
            item.setUnitPrice(p.getCurrentPrice());
            item.setQuantity(line.getQuantity());
            orderItemRepository.save(item);
            savedOrder.getItems().add(item);

            p.setStockQuantity(p.getStockQuantity() - line.getQuantity());
            productRepository.save(p);
        }

        customerService.refreshStatus(orderCustomer);
        cartService.clearCart(session, userId);

        if (session != null) {
            session.setAttribute(SESSION_LAST_ORDER_ID, savedOrder.getId());
        }

        emailService.sendOrderConfirmation(savedOrder);

        return savedOrder;
    }

    private Order handleUpdateExistingOrder(Long orderId, ReceiverForm form, List<CartLine> cartLines, HttpSession session, Long userId) {
        Order order = getCustomerOrderDetails(orderId, userId);
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new BusinessException("Chỉ được sửa đơn hàng khi đơn ở trạng thái 'Đã gửi đơn' (SUBMITTED).");
        }

        // Hoàn lại kho cũ
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product p = productRepository.findByIdWithLock(item.getProduct().getId()).orElse(null);
                if (p != null) {
                    p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                    productRepository.save(p);
                }
            }
        }

        // Kiểm tra tồn kho mới và trừ kho
        BigDecimal newTotal = BigDecimal.ZERO;
        List<Product> lockedProducts = new ArrayList<>();
        for (CartLine line : cartLines) {
            Product p = productRepository.findByIdWithLock(line.getProductId())
                    .orElseThrow(() -> new OutOfStockException(line.getName()));
            if (!p.isVisible() || p.getStockQuantity() < line.getQuantity()) {
                throw new OutOfStockException(p.getName());
            }
            lockedProducts.add(p);
            newTotal = newTotal.add(p.getCurrentPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }

        orderItemRepository.deleteByOrderId(order.getId());
        order.getItems().clear();

        for (int i = 0; i < cartLines.size(); i++) {
            CartLine line = cartLines.get(i);
            Product p = lockedProducts.get(i);

            OrderItem newItem = new OrderItem();
            newItem.setOrder(order);
            newItem.setProduct(p);
            newItem.setProductCode(p.getCode());
            newItem.setProductName(p.getName());
            newItem.setUnitPrice(p.getCurrentPrice());
            newItem.setQuantity(line.getQuantity());
            orderItemRepository.save(newItem);
            order.getItems().add(newItem);

            p.setStockQuantity(p.getStockQuantity() - line.getQuantity());
            productRepository.save(p);
        }

        order.setReceiverName(form.getFullName().trim());
        order.setReceiverGender(form.getGender());
        order.setReceiverEmail(form.getEmail().trim().toLowerCase());
        order.setReceiverMobile(form.getMobile().trim());
        order.setReceiverAddress(form.getAddress().trim());
        order.setNote(form.getNote() != null ? form.getNote().trim() : null);
        order.setTotalAmount(newTotal);

        customerService.upsertFromReceiver(form);

        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(session, userId);
        if (session != null) {
            session.removeAttribute(SESSION_EDITING_ORDER_ID);
            session.setAttribute(SESSION_LAST_ORDER_ID, savedOrder.getId());
        }

        emailService.sendOrderConfirmation(savedOrder);

        return savedOrder;
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng."));
    }

    @Override
    public Page<Order> getCustomerOrders(Long userId, int page) {
        return getCustomerOrders(userId, page, null, null);
    }

    @Override
    public Page<Order> getCustomerOrders(Long userId, int page, String status, String keyword) {
        Customer customer = customerService.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Tài khoản chưa có thông tin khách hàng."));

        int pageIndex = page > 0 ? page - 1 : 0;
        int defaultPageSize = (appProperties != null && appProperties.getPageSize() > 0) ? appProperties.getPageSize() : pageSize;
        Pageable pageable = PageRequest.of(pageIndex, defaultPageSize);

        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            try {
                orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        return orderRepository.searchCustomerOrders(customer.getId(), orderStatus, kw, pageable);
    }

    @Override
    @Transactional
    public Order getCustomerOrderDetails(Long orderId, Long userId) {
        Customer customer = customerService.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Tài khoản chưa có thông tin khách hàng."));

        Order order = orderRepository.findByIdAndCustomerId(orderId, customer.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng hoặc bạn không có quyền xem đơn này."));

        checkAndAutoAdvanceShipping(order);
        return order;
    }

    private void checkAndAutoAdvanceShipping(Order order) {
        if (order != null && order.getStatus() == OrderStatus.SHIPPING && order.getUpdatedAt() != null) {
            long minutes = java.time.Duration.between(order.getUpdatedAt(), java.time.LocalDateTime.now()).toMinutes();
            if (minutes >= 5) {
                order.setStatus(OrderStatus.SUCCESS);
                orderRepository.save(order);
                customerService.refreshStatus(order.getCustomer());
                log.info("Đơn hàng {} tự động chuyển sang SUCCESS (Hoàn tất) sau {} phút.", order.getCode(), minutes);
            }
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 30000)
    @Transactional
    public void autoAdvanceShippingOrders() {
        try {
            java.time.LocalDateTime fiveMinutesAgo = java.time.LocalDateTime.now().minusMinutes(5);
            List<Order> shippingOrders = orderRepository.findByStatusAndUpdatedAtBefore(OrderStatus.SHIPPING, fiveMinutesAgo);
            if (shippingOrders != null && !shippingOrders.isEmpty()) {
                for (Order order : shippingOrders) {
                    order.setStatus(OrderStatus.SUCCESS);
                    orderRepository.save(order);
                    customerService.refreshStatus(order.getCustomer());
                    log.info("Scheduler: Đơn hàng {} đã tự động hoàn tất giao hàng sau 5 phút.", order.getCode());
                }
            }
        } catch (Exception ex) {
            log.warn("Lỗi khi chạy autoAdvanceShippingOrders: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void prepareUpdateOrder(Long orderId, Long userId, HttpSession session) {
        Order order = getCustomerOrderDetails(orderId, userId);
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new BusinessException("Chỉ được sửa đơn hàng khi đơn ở trạng thái 'Đã gửi đơn' (SUBMITTED).");
        }

        cartService.loadOrderItemsToCart(session, userId, orderId);
        if (session != null) {
            session.setAttribute(SESSION_EDITING_ORDER_ID, orderId);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        cancelOrder(orderId, userId, null);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String cancelReason) {
        Order order = getCustomerOrderDetails(orderId, userId);
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            String msg = "Đơn hàng đã được xác nhận nên không thể hủy.";
            if (order.getStatus() == OrderStatus.SHIPPING) {
                msg = "Đơn hàng đang được giao nên không thể hủy.";
            } else if (order.getStatus() == OrderStatus.SUCCESS) {
                msg = "Đơn hàng đã hoàn tất nên không thể hủy.";
            } else if (order.getStatus() == OrderStatus.CANCELLED) {
                msg = "Đơn hàng này đã bị hủy trước đó.";
            }
            throw new BusinessException(msg);
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (cancelReason != null && !cancelReason.isBlank()) {
            order.setSaleNote("Lý do: " + cancelReason.trim());
        } else {
            order.setSaleNote("Lý do: Tôi muốn thay đổi nhu cầu mua sắm.");
        }

        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = productRepository.findByIdWithLock(item.getProduct().getId()).orElse(null);
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        orderRepository.save(order);
        customerService.refreshStatus(order.getCustomer());
    }

    @Override
    @Transactional
    public void editCustomerOrder(Long orderId, Long userId, com.swd392.onlineshop.dto.request.OrderEditRequest form) {
        Order order = getCustomerOrderDetails(orderId, userId);
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new BusinessException("Chỉ được sửa đơn hàng khi đơn ở trạng thái 'Đã gửi đơn' (SUBMITTED).");
        }

        if (form.getReceiverName() != null && !form.getReceiverName().isBlank()) {
            order.setReceiverName(form.getReceiverName().trim());
        }
        if (form.getReceiverMobile() != null && !form.getReceiverMobile().isBlank()) {
            order.setReceiverMobile(form.getReceiverMobile().trim());
        }
        if (form.getReceiverEmail() != null && !form.getReceiverEmail().isBlank()) {
            order.setReceiverEmail(form.getReceiverEmail().trim());
        }
        if (form.getReceiverAddress() != null && !form.getReceiverAddress().isBlank()) {
            order.setReceiverAddress(form.getReceiverAddress().trim());
        }
        order.setNote(form.getNote() != null ? form.getNote().trim() : null);

        java.util.Map<Long, Integer> qtyMap = new java.util.HashMap<>();
        if (form.getItemIds() != null && form.getQuantities() != null) {
            for (int i = 0; i < form.getItemIds().size(); i++) {
                if (i < form.getQuantities().size()) {
                    qtyMap.put(form.getItemIds().get(i), form.getQuantities().get(i));
                }
            }
        }

        java.util.Iterator<OrderItem> it = order.getItems().iterator();
        BigDecimal subtotal = BigDecimal.ZERO;
        while (it.hasNext()) {
            OrderItem item = it.next();
            if (!qtyMap.containsKey(item.getId()) || qtyMap.get(item.getId()) <= 0) {
                // Bi bo khoi don hang -> hoan lai ton kho
                if (item.getProduct() != null) {
                    Product p = productRepository.findByIdWithLock(item.getProduct().getId()).orElse(null);
                    if (p != null) {
                        p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                        productRepository.save(p);
                    }
                }
                it.remove();
            } else {
                int newQty = qtyMap.get(item.getId());
                int oldQty = item.getQuantity();
                int diff = newQty - oldQty;
                if (diff != 0 && item.getProduct() != null) {
                    Product p = productRepository.findByIdWithLock(item.getProduct().getId()).orElse(null);
                    if (p != null) {
                        if (diff > 0 && p.getStockQuantity() < diff) {
                            throw new BusinessException("Sản phẩm '" + item.getProductName() + "' chỉ còn " + p.getStockQuantity() + " trong kho, không đủ số lượng tăng thêm.");
                        }
                        p.setStockQuantity(p.getStockQuantity() - diff);
                        productRepository.save(p);
                    }
                }
                item.setQuantity(newQty);
                subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(newQty)));
            }
        }

        if (order.getItems().isEmpty()) {
            throw new BusinessException("Đơn hàng phải có ít nhất một sản phẩm.");
        }

        BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        order.setTotalAmount(subtotal.add(shippingFee));

        orderRepository.save(order);
    }

    private void assignRoundRobinSale(Order order) {
        try {
            List<Long> candidateIds = orderRepository.findRoundRobinSaleCandidates();
            if (candidateIds != null && !candidateIds.isEmpty()) {
                Long saleId = candidateIds.get(0);
                userRepository.findById(saleId).ifPresent(order::setAssignedSale);
            }
        } catch (Exception ex) {
            log.warn("Không thể tự động phân công Sale xoay vòng: {}", ex.getMessage());
        }
    }

    private String generateOrderCode() {
        long count = orderRepository.count();
        String code = String.format("OD%06d", count + 1);
        while (orderRepository.existsByCode(code)) {
            code = String.format("OD%06d", count + 1 + new Random().nextInt(10000));
        }
        return code;
    }

    // ==========================================
    // SALE HELPER METHODS
    // ==========================================

    private void changeStatus(Order order, OrderStatus next) {
        OrderStatus current = order.getStatus();
        if (current == next) {
            return;
        }
        if (!allowed(current, next)) {
            throw new BusinessException("Không chuyển được từ " + label(current) + " sang " + label(next) + ".");
        }
        order.setStatus(next);
        if (next == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        if (next == OrderStatus.SUCCESS || next == OrderStatus.CANCELLED) {
            customerService.refreshStatus(order.getCustomer().getId());
        }
    }

    private boolean allowed(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case SUBMITTED -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPING || next == OrderStatus.CANCELLED;
            case SHIPPING -> next == OrderStatus.SUCCESS || next == OrderStatus.CANCELLED;
            default -> false;
        };
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                continue;
            }
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    private void writeHistory(Order order, String content) {
        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setActor(currentUser());
        history.setContent(content);
        orderHistoryRepository.save(history);
    }

    private String updateContent(OrderStatus before, OrderStatus after, boolean noteChanged) {
        if (before == after) {
            return "Cập nhật ghi chú nội bộ.";
        }
        String content = "Chuyển trạng thái từ " + label(before) + " sang " + label(after) + ".";
        if (after == OrderStatus.CANCELLED) {
            content = content + " Đã hoàn tồn kho.";
        }
        if (noteChanged) {
            content = content + " Cập nhật ghi chú nội bộ.";
        }
        return content;
    }

    private Specification<Order> specification(OrderFilter filter, User viewer) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!isManager(viewer)) {
                predicates.add(cb.equal(root.get("assignedSale").get("id"), viewer.getId()));
            } else if (filter.getSaleId() != null) {
                predicates.add(cb.equal(root.get("assignedSale").get("id"), filter.getSaleId()));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFrom().atStartOfDay()));
            }
            if (filter.getTo() != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), filter.getTo().plusDays(1).atStartOfDay()));
            }
            String keyword = filter.getKeyword() == null ? "" : filter.getKeyword().trim();
            if (!keyword.isEmpty()) {
                Join<Order, Customer> customer = root.join("customer");
                String like = "%" + keyword.toLowerCase() + "%";
                Predicate byName = cb.like(cb.lower(customer.get("fullName")), like);
                Predicate byCode = cb.like(cb.lower(root.get("code")), like);
                predicates.add(cb.or(byName, byCode));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort sort(OrderFilter filter) {
        String field = switch (filter.getSort() == null ? "" : filter.getSort()) {
            case "customer" -> "customer.fullName";
            case "totalAmount" -> "totalAmount";
            case "status" -> "status";
            default -> "createdAt";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }

    private Map<Long, List<String>> productNames(List<Order> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = orders.stream().map(Order::getId).toList();
        Map<Long, List<String>> grouped = new LinkedHashMap<>();
        for (OrderItem item : orderItemRepository.findByOrderIdIn(ids)) {
            grouped.computeIfAbsent(item.getOrder().getId(), key -> new ArrayList<>()).add(item.getProductName());
        }
        return grouped;
    }

    private Order requireOrder(Long id) {
        return orderRepository.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng."));
    }

    private void requireCanHandle(Order order, User viewer) {
        if (isManager(viewer)) {
            return;
        }
        if (!SALE.equals(roleValue(viewer))
                || order.getAssignedSale() == null
                || !viewer.getId().equals(order.getAssignedSale().getId())) {
            throw new ForbiddenException("Bạn không được xem đơn này.");
        }
    }

    private User requireActiveSale(Long saleId) {
        User sale = userRepository.findById(saleId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy Sale."));
        if (!SALE.equals(roleValue(sale)) || sale.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Chỉ giao được cho Sale đang hoạt động.");
        }
        return sale;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new ForbiddenException("Bạn cần đăng nhập.");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ForbiddenException("Không tìm thấy tài khoản đang đăng nhập."));
    }

    private boolean isManager(User user) {
        return SALE_MANAGER.equals(roleValue(user));
    }

    private String roleValue(User user) {
        return user.getRole() == null ? "" : user.getRole().getValue();
    }

    private List<OrderStatus> nextStatuses(OrderStatus current) {
        return switch (current) {
            case SUBMITTED -> List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> List.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED);
            case SHIPPING -> List.of(OrderStatus.SUCCESS, OrderStatus.CANCELLED);
            default -> List.of();
        };
    }

    private String label(OrderStatus status) {
        return switch (status) {
            case SUBMITTED -> "Đã gửi đơn";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao";
            case SUCCESS -> "Hoàn tất";
            case CANCELLED -> "Đã hủy";
        };
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private OrderRow toDto(Order order, Collection<String> names) {
        String summary = "";
        if (!names.isEmpty()) {
            summary = names.iterator().next();
            if (names.size() > 1) {
                summary = summary + " và " + (names.size() - 1) + " SP khác";
            }
        }
        Customer customer = order.getCustomer();
        return OrderRow.builder()
                .id(order.getId())
                .code(order.getCode())
                .createdAt(order.getCreatedAt())
                .customerName(customer == null ? "" : customer.getFullName())
                .productSummary(summary)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }

    private OrderView toDto(Order order, List<OrderHistory> histories) {
        Customer customer = order.getCustomer();
        User sale = order.getAssignedSale();
        List<OrderStatus> next = nextStatuses(order.getStatus());
        return OrderView.builder()
                .id(order.getId())
                .code(order.getCode())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .customerName(customer == null ? "" : customer.getFullName())
                .customerEmail(customer == null ? "" : customer.getEmail())
                .customerMobile(customer == null ? "" : customer.getMobile())
                .assignedSaleId(sale == null ? null : sale.getId())
                .saleName(sale == null ? "Chưa phân công" : sale.getFullName())
                .receiverName(order.getReceiverName())
                .receiverGender(order.getReceiverGender())
                .receiverEmail(order.getReceiverEmail())
                .receiverMobile(order.getReceiverMobile())
                .receiverAddress(order.getReceiverAddress())
                .note(order.getNote())
                .saleNote(order.getSaleNote())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .updatable(!next.isEmpty())
                .nextStatuses(next)
                .items(order.getItems().stream().map(this::toDto).toList())
                .histories(histories.stream().map(this::toDto).toList())
                .build();
    }

    private OrderLineView toDto(OrderItem item) {
        Product product = item.getProduct();
        String category = "";
        String thumbnail = null;
        if (product != null) {
            thumbnail = product.getThumbnail();
            if (product.getCategory() != null) {
                category = product.getCategory().getName();
            }
        }
        return OrderLineView.builder()
                .productName(item.getProductName())
                .thumbnail(thumbnail)
                .categoryName(category)
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

    private OrderHistoryView toDto(OrderHistory history) {
        User actor = history.getActor();
        return OrderHistoryView.builder()
                .createdAt(history.getCreatedAt())
                .content(history.getContent())
                .actorName(actor == null ? null : actor.getFullName())
                .build();
    }

    private SaleOption toDto(User sale) {
        return SaleOption.builder()
                .id(sale.getId())
                .fullName(sale.getFullName())
                .build();
    }
}
