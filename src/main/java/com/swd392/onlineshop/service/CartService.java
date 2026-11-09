package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.response.CartLine;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface CartService {

    List<CartLine> getCartLines(HttpSession session, Long userId);

    void addToCart(HttpSession session, Long userId, Long productId, int quantity);

    void updateQuantity(HttpSession session, Long userId, Long productId, int quantity);

    void removeFromCart(HttpSession session, Long userId, Long productId);

    void clearCart(HttpSession session, Long userId);

    int getCartCount(HttpSession session, Long userId);

    void mergeSessionCartToDb(HttpSession session, Long userId);

    void loadOrderItemsToCart(HttpSession session, Long userId, Long orderId);
}

