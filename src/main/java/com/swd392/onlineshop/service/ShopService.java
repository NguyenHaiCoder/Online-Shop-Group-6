package com.swd392.onlineshop.service;
import com.swd392.onlineshop.dto.request.ProductFilter;
import com.swd392.onlineshop.dto.response.*;
import org.springframework.data.domain.Page;
import java.util.List;
public interface ShopService {
    HomeView home();
    List<SettingRow> categories();
    Page<ProductRow> products(ProductFilter filter,int page);
    ProductView product(Long id);
    Page<PostRow> posts(int page);
    PostView post(Long id);
}