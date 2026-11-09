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
    private String briefInfo;
    public static PostRow from(com.swd392.onlineshop.entity.Post p) {
        return PostRow.builder().id(p.getId()).code(p.getCode()).title(p.getTitle()).thumbnail(p.getThumbnail())
            .briefInfo(p.getBriefInfo()).authorName(p.getAuthor()==null?null:p.getAuthor().getFullName())
            .updatedAt(p.getUpdatedAt()).visible(p.isVisible()).build();
    }
}
