package com.swd392.onlineshop.service.impl;
import com.swd392.onlineshop.dto.request.CatalogFilter;
import com.swd392.onlineshop.dto.response.*;
import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.enums.SettingType;
import com.swd392.onlineshop.repository.*;
import com.swd392.onlineshop.service.ShopService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ShopServiceImpl implements ShopService {
    private final ProductRepository products;
    private final PostRepository posts;
    private final SettingRepository settings;
    private final SliderRepository sliders;
    @Override public List<SettingRow> categories() {
        return settings.findByTypeOrderByDisplayOrderAscIdAsc(SettingType.PRODUCT_CATEGORY).stream()
            .filter(Setting::isActive).map(SettingRow::from).toList();
    }
    @Override public HomeView home() {
        return new HomeView(products.findByVisibleTrueAndFeaturedTrueOrderByUpdatedAtDescIdDesc(PageRequest.of(0,3)).stream().map(ProductRow::from).toList(),
            posts.findByVisibleTrueAndFeaturedTrueOrderByUpdatedAtDescIdDesc(PageRequest.of(0,2)).stream().map(PostRow::from).toList(),categories(),
            sliders.findByVisibleTrueOrderByDisplayOrderAscIdAsc().stream().map(s->new HomeView.SliderView(s.getTitle(),s.getImage(),s.getNotes(),s.getButtonLabel(),safeLink(s.getBacklink()))).toList());
    }
    private String safeLink(String link) { return link!=null && link.startsWith("/") && !link.startsWith("//") ? link : "/products"; }
    @Override public Page<ProductRow> products(CatalogFilter f,int page) {
        return products.findAll((root,q,cb)->{
            List<Predicate> p=new ArrayList<>(); p.add(cb.isTrue(root.get("visible")));
            if(f.getKeyword()!=null && !f.getKeyword().isBlank()) {
                String k="%"+f.getKeyword().trim().toLowerCase(Locale.ROOT)+"%";
                p.add(cb.or(cb.like(cb.lower(root.get("name")),k),cb.like(cb.lower(root.get("code")),k)));
            }
            if(f.getCategoryId()!=null && !f.getCategoryId().isEmpty()) p.add(root.get("category").get("id").in(f.getCategoryId()));
            if(f.getMinPrice()!=null) p.add(cb.greaterThanOrEqualTo(root.get("originalPrice"),f.getMinPrice()));
            if(f.getMaxPrice()!=null) p.add(cb.lessThanOrEqualTo(root.get("originalPrice"),f.getMaxPrice()));
            return cb.and(p.toArray(Predicate[]::new));
        },PageRequest.of(Math.max(0,page-1),9,Sort.by(Sort.Direction.DESC,"updatedAt","id"))).map(ProductRow::from);
    }
    @Override public ProductView product(Long id) { return ProductView.from(products.findByIdAndVisibleTrue(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND))); }
    @Override public Page<PostRow> posts(int page) { return posts.findByVisibleTrueOrderByUpdatedAtDescIdDesc(PageRequest.of(Math.max(0,page-1),9)).map(PostRow::from); }
    @Override public PostView post(Long id) { return PostView.from(posts.findByIdAndVisibleTrue(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND))); }
}