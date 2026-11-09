package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Mot dong cua danh sach bai viet (MP01). */
@Getter
@Builder
@AllArgsConstructor
public class PostRow {

    private Long id;
    private String code;
    private String title;
    private String thumbnail;
    private String authorName;
    private LocalDateTime updatedAt;
    private boolean visible;
}
