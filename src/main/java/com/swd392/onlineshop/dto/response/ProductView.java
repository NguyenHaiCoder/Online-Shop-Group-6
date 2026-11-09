package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Man chi tiet san pham (PR03), dung lai o form sua (PR04) de hien anh. */
@Getter
@Builder
@AllArgsConstructor
public class ProductView {

    private Long id;
    private String code;
    private String name;
    private String thumbnail;
    private String categoryName;
    private BigDecimal currentPrice;
    private int stockQuantity;
    private boolean visible;
    private String briefInfo;
    private String description;
    /** Moi phan tu dang "Ten: Gia tri". */
    private List<String> specifications;
    /** id anh gallery -> URL, theo thu tu hien thi. */
    private Map<Long, String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
