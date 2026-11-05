package com.swd392.onlineshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bai viet / Blog.
 * Public: View Blog List, View Blog Details.
 * Marketing: View Post List, Add/Edit/Show/Hide Post.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String thumbnail;

    @Column(length = 500)
    private String briefInfo;

    @Lob
    private String content;

    /** Setting co type = POST_CATEGORY */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Setting category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false)
    private boolean featured = false;

    /** Show/Hide Post */
    @Column(nullable = false)
    private boolean visible = true;
}
