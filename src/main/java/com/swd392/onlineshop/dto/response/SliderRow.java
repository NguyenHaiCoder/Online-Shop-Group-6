package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Mot dong cua danh sach slider (MS01). */
@Getter
@Builder
@AllArgsConstructor
public class SliderRow {

    private Long id;
    private String title;
    private String image;
    private String backlink;
    private Integer displayOrder;
    private boolean visible;
}
