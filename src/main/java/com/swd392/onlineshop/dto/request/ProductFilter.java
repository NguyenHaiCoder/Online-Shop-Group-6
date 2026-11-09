package com.swd392.onlineshop.dto.request;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
@Getter @Setter
public class ProductFilter {
    private String keyword;
    private List<Long> categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}