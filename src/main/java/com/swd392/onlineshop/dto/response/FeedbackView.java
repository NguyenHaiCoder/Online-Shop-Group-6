package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/** Man chi tiet phan hoi (MF03), dung lai o form sua (MF04). */
@Getter
@Builder
@AllArgsConstructor
public class FeedbackView {

    private Long id;
    private String code;
    private String source;
    private String fullName;
    private String email;
    private String subject;
    private String content;
    private boolean visible;
    private Long productId;
    private String productCode;
    private String productName;
    private String productThumbnail;
    private List<String> imageUrls;
    private String updatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
