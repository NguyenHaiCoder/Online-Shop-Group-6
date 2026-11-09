package com.swd392.onlineshop.dto.response;
import com.swd392.onlineshop.entity.Post;
import java.time.LocalDateTime;
public record PostRow(Long id,String title,String thumbnail,String briefInfo,LocalDateTime updatedAt) {
    public static PostRow from(Post p) { return new PostRow(p.getId(),p.getTitle(),p.getThumbnail(),p.getBriefInfo(),p.getUpdatedAt()); }
}