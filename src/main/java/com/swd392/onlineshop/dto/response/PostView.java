package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Man chi tiet bai viet (MP03). */
@Getter
@Builder
@AllArgsConstructor
public class PostView {

    private Long id;
    private String code;
    private String title;
    private String thumbnail;
    private String briefInfo;
    private String content;
    private String authorName;
    private boolean visible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
