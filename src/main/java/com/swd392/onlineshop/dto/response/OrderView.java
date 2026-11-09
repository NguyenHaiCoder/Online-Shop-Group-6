package com.swd392.onlineshop.dto.response;

import com.swd392.onlineshop.enums.Gender;
import com.swd392.onlineshop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Chi tiet don cho Sale va Sale Manager. */
@Getter
@Builder
@AllArgsConstructor
public class OrderView {

    private Long id;
    private String code;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String customerName;
    private String customerEmail;
    private String customerMobile;
    private Long assignedSaleId;
    private String saleName;
    private String receiverName;
    private Gender receiverGender;
    private String receiverEmail;
    private String receiverMobile;
    private String receiverAddress;
    private String note;
    private String saleNote;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private boolean updatable;
    private List<OrderStatus> nextStatuses;
    private List<OrderLineView> items;
    private List<OrderHistoryView> histories;
}
