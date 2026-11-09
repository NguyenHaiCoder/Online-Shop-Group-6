package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.AssignOrderForm;
import com.swd392.onlineshop.dto.request.OrderFilter;
import com.swd392.onlineshop.dto.request.OrderUpdateForm;
import com.swd392.onlineshop.dto.response.OrderRow;
import com.swd392.onlineshop.dto.response.OrderView;
import com.swd392.onlineshop.dto.response.SaleOption;
import org.springframework.data.domain.Page;

import java.util.List;

/** Don hang cua Sale (don duoc giao) va Sale Manager (moi don). */
public interface OrderService {

    Page<OrderRow> list(OrderFilter filter);

    OrderView getDetail(Long id);

    void update(Long id, OrderUpdateForm form);

    void assign(Long id, AssignOrderForm form);

    List<SaleOption> activeSales();

    /** Sale Manager dang xem toan bo don. Sale thi khong. */
    boolean viewingAll();
}
