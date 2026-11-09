package com.swd392.onlineshop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/** Tim kiem / loc danh sach san pham (PR01 va Public Catalog). */
@Getter
@Setter
@NoArgsConstructor
public class ProductFilter {

    /** Tim theo ten hoac ma san pham. */
    private String keyword;
    private Long categoryId;
    private List<Long> categoryIds;
    private Boolean visible;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort;
    private Integer page = 1;

    public List<Long> getEffectiveCategoryIds() {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return categoryIds;
        }
        if (categoryId != null) {
            return List.of(categoryId);
        }
        return Collections.emptyList();
    }
}
