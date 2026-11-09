package com.swd392.onlineshop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tim kiem / loc danh sach bai viet (MP01 va Public Blog). */
@Getter
@Setter
@NoArgsConstructor
public class PostFilter {

    /** Tim theo tieu de, ma bai viet hoac email tac gia. */
    private String keyword;
    private Long categoryId;
    private Boolean visible;
    private Integer page = 1;
}
