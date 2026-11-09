package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.PostFilter;
import com.swd392.onlineshop.dto.request.PostForm;
import com.swd392.onlineshop.dto.response.PostRow;
import com.swd392.onlineshop.dto.response.PostView;
import org.springframework.data.domain.Page;

/**
 * Marketing: View Post List, Add New Post, View Post Details,
 * Edit Post, Show/Hide Post (MP01-MP05).
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
}
