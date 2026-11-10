package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.AssignOrderForm;
import com.swd392.onlineshop.dto.request.OrderFilter;
import com.swd392.onlineshop.dto.request.OrderUpdateForm;
import com.swd392.onlineshop.dto.request.ReceiverForm;
import com.swd392.onlineshop.dto.response.OrderRow;
import com.swd392.onlineshop.dto.response.OrderView;
import com.swd392.onlineshop.dto.response.SaleOption;
import com.swd392.onlineshop.entity.Order;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Don hang cua Sale (don duoc giao), Sale Manager (moi don) va Khach hang (My Orders / Checkout).
 */
public interface OrderService {

    Page<OrderRow> list(OrderFilter filter);

    OrderView getDetail(Long id);

    void update(Long id, OrderUpdateForm form);

    void assign(Long id, AssignOrderForm form);

    List<SaleOption> activeSales();

    /** Sale Manager dang xem toan bo don. Sale thi khong. */
    boolean viewingAll();

    Order placeOrder(ReceiverForm form, HttpSession session, Long userId);

    Order getOrderById(Long orderId);

    Page<Order> getCustomerOrders(Long userId, int page);

    Page<Order> getCustomerOrders(Long userId, int page, String status, String keyword);

    Order getCustomerOrderDetails(Long orderId, Long userId);

    void prepareUpdateOrder(Long orderId, Long userId, HttpSession session);

    void cancelOrder(Long orderId, Long userId);

    void cancelOrder(Long orderId, Long userId, String cancelReason);

    void editCustomerOrder(Long orderId, Long userId, com.swd392.onlineshop.dto.request.OrderEditRequest form);
}
