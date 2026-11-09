package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.PostFilter;
import com.swd392.onlineshop.dto.request.PostForm;
import com.swd392.onlineshop.dto.response.PostRow;
import com.swd392.onlineshop.dto.response.PostView;
import com.swd392.onlineshop.entity.Post;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Marketing: View Post List, Add New Post, View Post Details,
 * Edit Post, Show/Hide Post (MP01-MP05).
 * Public: Hot posts, latest posts, blog list, blog detail.
 */
public interface PostService {

    /** page bat dau tu 1. */
    Page<PostRow> search(PostFilter filter, int page);

    PostView getView(Long id);

    PostForm getForm(Long id);

    /** authorEmail: Marketing dang dang nhap. */
    Long create(PostForm form, String authorEmail);

    void update(Long id, PostForm form);

    /** Doi Hien/An, tra ve trang thai moi. */
    boolean toggleVisible(Long id);

    List<Post> getFeaturedPosts(int limit);

    List<Post> getLatestPosts(int limit);

    Page<Post> getPublicPosts(PostFilter filter);

    Post getPublicPostById(Long id);
}
