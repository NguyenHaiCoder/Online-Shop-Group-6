package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.FeedbackFilter;
import com.swd392.onlineshop.dto.request.FeedbackForm;
import com.swd392.onlineshop.dto.response.FeedbackRow;
import com.swd392.onlineshop.dto.response.FeedbackView;
import org.springframework.data.domain.Page;

/**
 * Marketing: View Feedback List, Add New Feedback, View Feedback Details,
 * Edit Feedback, Show/Hide Feedback (MF01-MF05).
 */
public interface FeedbackService {

    /** page bat dau tu 1. */
    Page<FeedbackRow> search(FeedbackFilter filter, int page);

    FeedbackView getView(Long id);

    FeedbackForm getForm(Long id);

    /** Phan hoi phai gan voi khach hang co san cung email. */
    boolean customerExists(String email);

    Long create(FeedbackForm form);

    /** actorEmail: Marketing dang dang nhap, luu vao updatedBy. */
    void update(Long id, FeedbackForm form, String actorEmail);

    /** Doi Hien/An, tra ve trang thai moi. */
    boolean toggleVisible(Long id, String actorEmail);
}
