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
    private String categoryName;
    public static PostView from(com.swd392.onlineshop.entity.Post p) {
        return PostView.builder().id(p.getId()).code(p.getCode()).title(p.getTitle()).thumbnail(p.getThumbnail())
            .briefInfo(p.getBriefInfo()).content(p.getContent()).authorName(p.getAuthor()==null?null:p.getAuthor().getFullName())
            .categoryName(p.getCategory()==null?null:p.getCategory().getName()).visible(p.isVisible())
            .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt()).build();
    }
}
