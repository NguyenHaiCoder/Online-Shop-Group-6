package com.swd392.onlineshop.dto.response;
import com.swd392.onlineshop.entity.Post;
import java.time.LocalDateTime;
public record PostView(Long id,String title,String thumbnail,String briefInfo,String content,LocalDateTime updatedAt,
                       SettingRow category,Author author) {
    public record Author(String fullName) {}
    public static PostView from(Post p) { return new PostView(p.getId(),p.getTitle(),p.getThumbnail(),p.getBriefInfo(),p.getContent(),p.getUpdatedAt(),
        p.getCategory()==null?null:SettingRow.from(p.getCategory()),p.getAuthor()==null?null:new Author(p.getAuthor().getFullName())); }
}