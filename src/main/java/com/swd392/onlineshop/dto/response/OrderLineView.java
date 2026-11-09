package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** Mot dong hang trong chi tiet don. Gia va ten la ban chup luc dat. */
@Getter
@Builder
@AllArgsConstructor
public class OrderLineView {

    private String productName;
    private String thumbnail;
    private String categoryName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
}
