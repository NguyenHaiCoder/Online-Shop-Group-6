package com.swd392.onlineshop.dto.response;

import com.swd392.onlineshop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Mot dong tren danh sach don. */
@Getter
@Builder
@AllArgsConstructor
public class OrderRow {

    private Long id;
    private String code;
    private LocalDateTime createdAt;
    private String customerName;
    private String productSummary;
    private BigDecimal totalAmount;
    private OrderStatus status;
}
