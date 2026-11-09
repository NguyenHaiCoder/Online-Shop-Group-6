package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** Mot dong cua danh sach san pham (PR01). */
@Getter
@Builder
@AllArgsConstructor
public class ProductRow {

    private Long id;
    private String code;
    private String name;
    private String thumbnail;
    private String categoryName;
    private BigDecimal currentPrice;
    private int stockQuantity;
    private boolean visible;
    public static ProductRow from(com.swd392.onlineshop.entity.Product p) {
        return ProductRow.builder().id(p.getId()).code(p.getCode()).name(p.getName()).thumbnail(p.getThumbnail())
            .categoryName(p.getCategory()==null?null:p.getCategory().getName()).currentPrice(p.getOriginalPrice())
            .stockQuantity(p.getStockQuantity()).visible(p.isVisible()).build();
    }
}
