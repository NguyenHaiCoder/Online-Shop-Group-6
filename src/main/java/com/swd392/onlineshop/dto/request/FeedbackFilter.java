package com.swd392.onlineshop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tim kiem / loc danh sach phan hoi (MF01). */
@Getter
@Setter
@NoArgsConstructor
public class FeedbackFilter {

    /** Tim theo ten, ma hoac email nguoi gui. */
    private String keyword;
    private Boolean visible;
}
