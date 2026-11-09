package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartLine {

    private Long productId;
    private String productCode;
    private String name;
    private String thumbnail;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;
    private String warning;
}

