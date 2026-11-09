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
    private SettingRow category;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    public static ProductView from(com.swd392.onlineshop.entity.Product p) {
        Map<Long,String> images = new java.util.LinkedHashMap<>();
        p.getImages().forEach(i -> images.put(i.getId(),i.getImageUrl()));
        return ProductView.builder().id(p.getId()).code(p.getCode()).name(p.getName()).thumbnail(p.getThumbnail())
            .categoryName(p.getCategory()==null?null:p.getCategory().getName())
            .category(p.getCategory()==null?null:SettingRow.from(p.getCategory())).currentPrice(p.getOriginalPrice())
            .originalPrice(p.getOriginalPrice()).stockQuantity(p.getStockQuantity()).visible(p.isVisible())
            .briefInfo(p.getBriefInfo()).description(p.getDescription()).images(images)
            .specifications(p.getSpecifications().stream().map(s -> s.getName()+": "+s.getValue()).toList())
            .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt()).build();
    }
}
