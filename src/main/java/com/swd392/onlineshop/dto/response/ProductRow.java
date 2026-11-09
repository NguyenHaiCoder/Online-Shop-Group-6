package com.swd392.onlineshop.dto.response;
import com.swd392.onlineshop.entity.Product;
import java.math.BigDecimal;
public record ProductRow(Long id,String code,String name,String thumbnail,BigDecimal currentPrice,int stockQuantity) {
    public static ProductRow from(Product p) {
        return new ProductRow(p.getId(),p.getCode(),p.getName(),p.getThumbnail(),p.getOriginalPrice(),p.getStockQuantity());
    }
}