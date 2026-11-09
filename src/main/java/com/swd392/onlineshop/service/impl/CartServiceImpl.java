package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.response.CartLine;
import com.swd392.onlineshop.entity.Cart;
import com.swd392.onlineshop.entity.CartItem;
import com.swd392.onlineshop.entity.Order;
import com.swd392.onlineshop.entity.OrderItem;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.exception.NotFoundException;
import com.swd392.onlineshop.repository.CartItemRepository;
import com.swd392.onlineshop.repository.CartRepository;
import com.swd392.onlineshop.repository.OrderRepository;
import com.swd392.onlineshop.repository.ProductRepository;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    public static final String SESSION_CART_KEY = "CART";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CartLine> getCartLines(HttpSession session, Long userId) {
        Map<Long, Integer> items = getCartMap(session, userId);
        List<CartLine> lines = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                continue;
            }
            Product product = productOpt.get();
            BigDecimal price = product.getCurrentPrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));

            String warning = null;
            if (!product.isVisible()) {
                warning = "Sản phẩm tạm ngừng kinh doanh";
            } else if (product.getStockQuantity() <= 0) {
                warning = "Sản phẩm đã hết hàng";
            } else if (product.getStockQuantity() < quantity) {
                warning = "Kho chỉ còn " + product.getStockQuantity() + " sản phẩm";
            }

            lines.add(CartLine.builder()
                    .productId(product.getId())
                    .productCode(product.getCode())
                    .name(product.getName())
                    .thumbnail(product.getThumbnail())
                    .unitPrice(price)
                    .quantity(quantity)
                    .lineTotal(lineTotal)
                    .warning(warning)
                    .build());
        }

        return lines;
    }

    @Override
    @Transactional
    public void addToCart(HttpSession session, Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm."));

        if (userId != null) {
            Cart cart = getOrCreateDbCart(userId);
            Optional<CartItem> itemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
            if (itemOpt.isPresent()) {
                CartItem item = itemOpt.get();
                item.setQuantity(item.getQuantity() + quantity);
                cartItemRepository.save(item);
            } else {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
        } else {
            LinkedHashMap<Long, Integer> sessionCart = getSessionCart(session);
            sessionCart.put(productId, sessionCart.getOrDefault(productId, 0) + quantity);
            session.setAttribute(SESSION_CART_KEY, sessionCart);
        }
    }

    @Override
    @Transactional
    public void updateQuantity(HttpSession session, Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(session, userId, productId);
            return;
        }
        if (userId != null) {
            Cart cart = getOrCreateDbCart(userId);
            cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).ifPresent(item -> {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            });
        } else {
            LinkedHashMap<Long, Integer> sessionCart = getSessionCart(session);
            if (sessionCart.containsKey(productId)) {
                sessionCart.put(productId, quantity);
                session.setAttribute(SESSION_CART_KEY, sessionCart);
            }
        }
    }

    @Override
    @Transactional
    public void removeFromCart(HttpSession session, Long userId, Long productId) {
        if (userId != null) {
            cartRepository.findByUserId(userId).ifPresent(cart ->
                    cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                            .ifPresent(cartItemRepository::delete));
        } else {
            LinkedHashMap<Long, Integer> sessionCart = getSessionCart(session);
            sessionCart.remove(productId);
            session.setAttribute(SESSION_CART_KEY, sessionCart);
        }
    }

    @Override
    @Transactional
    public void clearCart(HttpSession session, Long userId) {
        if (userId != null) {
            cartRepository.findByUserId(userId).ifPresent(cart ->
                    cartItemRepository.deleteByCartId(cart.getId()));
        }
        if (session != null) {
            session.removeAttribute(SESSION_CART_KEY);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartCount(HttpSession session, Long userId) {
        if (userId != null) {
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
            return cartOpt.map(cart -> cart.getItems().stream().mapToInt(CartItem::getQuantity).sum()).orElse(0);
        }
        if (session == null) {
            return 0;
        }
        LinkedHashMap<Long, Integer> sessionCart = getSessionCart(session);
        return sessionCart.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    @Transactional
    public void mergeSessionCartToDb(HttpSession session, Long userId) {
        if (userId == null || session == null) {
            return;
        }
        LinkedHashMap<Long, Integer> sessionCart = getSessionCart(session);
        if (sessionCart.isEmpty()) {
            return;
        }
        Cart cart = getOrCreateDbCart(userId);

        for (Map.Entry<Long, Integer> entry : sessionCart.entrySet()) {
            Long productId = entry.getKey();
            int addQty = entry.getValue();

            Optional<Product> prodOpt = productRepository.findById(productId);
            if (prodOpt.isEmpty()) {
                continue;
            }
            Product prod = prodOpt.get();

            Optional<CartItem> itemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
            if (itemOpt.isPresent()) {
                CartItem item = itemOpt.get();
                int newQty = Math.min(item.getQuantity() + addQty, Math.max(1, prod.getStockQuantity()));
                item.setQuantity(newQty);
                cartItemRepository.save(item);
            } else {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProduct(prod);
                item.setQuantity(Math.min(addQty, Math.max(1, prod.getStockQuantity())));
                cartItemRepository.save(item);
            }
        }
        session.removeAttribute(SESSION_CART_KEY);
    }

    @Override
    @Transactional
    public void loadOrderItemsToCart(HttpSession session, Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng."));

        if (userId != null) {
            Cart cart = getOrCreateDbCart(userId);
            cartItemRepository.deleteByCartId(cart.getId());

            for (OrderItem oi : order.getItems()) {
                if (oi.getProduct() != null) {
                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProduct(oi.getProduct());
                    ci.setQuantity(oi.getQuantity());
                    cartItemRepository.save(ci);
                }
            }
        } else {
            LinkedHashMap<Long, Integer> newCart = new LinkedHashMap<>();
            for (OrderItem oi : order.getItems()) {
                if (oi.getProduct() != null) {
                    newCart.put(oi.getProduct().getId(), oi.getQuantity());
                }
            }
            session.setAttribute(SESSION_CART_KEY, newCart);
        }
    }

    private Cart getOrCreateDbCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng."));
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<Long, Integer> getSessionCart(HttpSession session) {
        if (session == null) {
            return new LinkedHashMap<>();
        }
        Object attr = session.getAttribute(SESSION_CART_KEY);
        if (attr instanceof LinkedHashMap) {
            return (LinkedHashMap<Long, Integer>) attr;
        }
        LinkedHashMap<Long, Integer> map = new LinkedHashMap<>();
        session.setAttribute(SESSION_CART_KEY, map);
        return map;
    }

    private Map<Long, Integer> getCartMap(HttpSession session, Long userId) {
        if (userId != null) {
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isPresent()) {
                Map<Long, Integer> map = new LinkedHashMap<>();
                for (CartItem item : cartOpt.get().getItems()) {
                    map.put(item.getProduct().getId(), item.getQuantity());
                }
                return map;
            }
            return Map.of();
        }
        if (session == null) {
            return Map.of();
        }
        return getSessionCart(session);
    }
}

