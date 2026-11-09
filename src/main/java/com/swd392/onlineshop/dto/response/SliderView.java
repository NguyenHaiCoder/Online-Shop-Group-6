package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Man chi tiet slider (MS03). */
@Getter
@Builder
@AllArgsConstructor
public class SliderView {

    private Long id;
    private String title;
    private String image;
    private String notes;
    private String buttonLabel;
    private String backlink;
    private Integer displayOrder;
    private boolean visible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
