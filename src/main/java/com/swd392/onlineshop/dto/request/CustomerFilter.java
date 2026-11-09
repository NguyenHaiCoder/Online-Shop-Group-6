package com.swd392.onlineshop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tim kiem / loc danh sach khach hang (MC01). */
@Getter
@Setter
@NoArgsConstructor
public class CustomerFilter {

    /** Tim theo ho ten, ma khach hoac email. */
    private String keyword;
    private Boolean visible;
}
