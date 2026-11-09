package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Mot dong cua danh sach phan hoi (MF01). */
@Getter
@Builder
@AllArgsConstructor
public class FeedbackRow {

    private Long id;
    private String code;
    private String fullName;
    private String email;
    private String subject;
    private LocalDateTime createdAt;
    private boolean visible;
}
