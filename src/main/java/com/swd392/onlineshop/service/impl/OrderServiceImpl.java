package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.request.AssignOrderForm;
import com.swd392.onlineshop.dto.request.OrderFilter;
import com.swd392.onlineshop.dto.request.OrderUpdateForm;
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
import com.swd392.onlineshop.repository.OrderHistoryRepository;
import com.swd392.onlineshop.repository.OrderItemRepository;
import com.swd392.onlineshop.repository.OrderRepository;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.CustomerService;
import com.swd392.onlineshop.service.OrderService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final String SALE = "SALE";
    private static final String SALE_MANAGER = "SALE_MANAGER";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final UserRepository userRepository;
    private final CustomerService customerService;

    @Value("${app.page-size:10}")
    private int pageSize;

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
