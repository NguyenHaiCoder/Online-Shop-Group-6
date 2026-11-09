package com.swd392.onlineshop.dto.response;
import com.swd392.onlineshop.entity.Product;
import java.math.BigDecimal;
import java.util.List;
public record ProductView(Long id,String code,String name,String thumbnail,String briefInfo,String description,
    BigDecimal currentPrice,BigDecimal originalPrice,BigDecimal salePrice,int stockQuantity,SettingRow category,
    List<Image> images,List<Specification> specifications) {
    public record Image(String imageUrl) {}
    public record Specification(String name,String value) {}
    public static ProductView from(Product p) {
        return new ProductView(p.getId(),p.getCode(),p.getName(),p.getThumbnail(),p.getBriefInfo(),p.getDescription(),
            p.getOriginalPrice(),p.getOriginalPrice(),null,p.getStockQuantity(),p.getCategory()==null?null:SettingRow.from(p.getCategory()),
            p.getImages().stream().map(i->new Image(i.getImageUrl())).toList(),
            p.getSpecifications().stream().map(s->new Specification(s.getName(),s.getValue())).toList());
    }
}