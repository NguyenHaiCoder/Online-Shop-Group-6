package com.swd392.onlineshop.dto.response;
import java.util.List;
public record HomeView(List<ProductRow> featuredProducts,List<PostRow> hotPosts,List<SettingRow> categories,List<SliderView> sliders) {
    public record SliderView(String title,String image,String notes,String buttonLabel,String backlink) {}
}