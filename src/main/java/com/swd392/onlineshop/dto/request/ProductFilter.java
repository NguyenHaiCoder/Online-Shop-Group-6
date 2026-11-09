package com.swd392.onlineshop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tim kiem / loc danh sach san pham (PR01). */
@Getter
@Setter
@NoArgsConstructor
public class ProductFilter {

    /** Tim theo ten hoac ma san pham. */
    private String keyword;
    private Long categoryId;
    private Boolean visible;
}
