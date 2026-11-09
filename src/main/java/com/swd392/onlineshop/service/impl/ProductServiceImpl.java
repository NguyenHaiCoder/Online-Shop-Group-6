package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.config.AppProperties;
import com.swd392.onlineshop.dto.request.ProductFilter;
import com.swd392.onlineshop.dto.request.ProductForm;
import com.swd392.onlineshop.dto.response.ProductRow;
import com.swd392.onlineshop.dto.response.ProductView;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.entity.ProductImage;
import com.swd392.onlineshop.entity.ProductSpecification;
import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.enums.SettingType;
import com.swd392.onlineshop.exception.NotFoundException;
import com.swd392.onlineshop.repository.ProductRepository;
import com.swd392.onlineshop.repository.SettingRepository;
import com.swd392.onlineshop.service.FileStorageService;
import com.swd392.onlineshop.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String FOLDER = "products";

    private final ProductRepository productRepository;
    private final SettingRepository settingRepository;
    private final FileStorageService fileStorageService;
    private final AppProperties appProperties;

    @Value("${app.page-size:10}")
    private int pageSize;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductRow> search(ProductFilter filter, int page) {
        String keyword = trimToNull(filter.getKeyword());
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(cb.like(root.get("name"), like), cb.like(root.get("code"), like)));
            }
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }
            if (filter.getVisible() != null) {
                predicates.add(cb.equal(root.get("visible"), filter.getVisible()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize,
                Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id")));
        return productRepository.findAll(spec, pageable).map(this::toRow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Setting> findCategories() {
        return settingRepository.findByTypeAndActiveTrueOrderByDisplayOrderAsc(SettingType.PRODUCT_CATEGORY);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRow> findAllForSelect() {
        return productRepository.findAllByOrderByNameAsc().stream()
                .map(p -> ProductRow.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        .thumbnail(p.getThumbnail())
                        .currentPrice(p.getCurrentPrice())
                        .stockQuantity(p.getStockQuantity())
                        .visible(p.isVisible())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductView getView(Long id) {
        Product p = find(id);
        Map<Long, String> images = new LinkedHashMap<>();
        for (ProductImage image : p.getImages()) {
            images.put(image.getId(), image.getImageUrl());
        }
        List<String> specs = p.getSpecifications().stream()
                .map(s -> s.getName() + ": " + s.getValue())
                .toList();
        return ProductView.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .thumbnail(p.getThumbnail())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .currentPrice(p.getCurrentPrice())
                .stockQuantity(p.getStockQuantity())
                .visible(p.isVisible())
                .briefInfo(p.getBriefInfo())
                .description(p.getDescription())
                .specifications(specs)
                .images(images)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductForm getForm(Long id) {
        Product p = find(id);
        ProductForm form = new ProductForm();
        form.setCode(p.getCode());
        form.setName(p.getName());
        form.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
        // Cot DB scale 2 (690000.00); form chi nhan so nguyen
        form.setOriginalPrice(p.getCurrentPrice().setScale(0, RoundingMode.HALF_UP));
        form.setStockQuantity(p.getStockQuantity());
        form.setVisible(p.isVisible());
        form.setBriefInfo(p.getBriefInfo());
        form.setDescription(p.getDescription());
        form.setSpecifications(p.getSpecifications().stream()
                .map(s -> s.getName() + ": " + s.getValue())
                .collect(Collectors.joining("\n")));
        return form;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean codeExists(String code, Long excludeId) {
        String value = trimToNull(code);
        if (value == null) {
            return false;
        }
        return excludeId == null
                ? productRepository.existsByCodeIgnoreCase(value)
                : productRepository.existsByCodeIgnoreCaseAndIdNot(value, excludeId);
    }

    @Override
    @Transactional
    public Long create(ProductForm form) {
        Setting category = requireCategory(form.getCategoryId());
        List<String[]> specs = parseSpecifications(form.getSpecifications());
        Product p = new Product();
        p.setThumbnail(fileStorageService.store(form.getThumbnailFile(), FOLDER));
        apply(p, form, category, specs);
        addGallery(p, form.getGalleryFiles());
        return productRepository.save(p).getId();
    }

    @Override
    @Transactional
    public void update(Long id, ProductForm form) {
        Product p = find(id);
        Setting category = requireCategory(form.getCategoryId());
        List<String[]> specs = parseSpecifications(form.getSpecifications());
        if (hasFile(form.getThumbnailFile())) {
            p.setThumbnail(fileStorageService.store(form.getThumbnailFile(), FOLDER));
        }
        apply(p, form, category, specs);
        if (form.getDeleteImageIds() != null && !form.getDeleteImageIds().isEmpty()) {
            p.getImages().removeIf(image -> form.getDeleteImageIds().contains(image.getId()));
        }
        addGallery(p, form.getGalleryFiles());
    }

    @Override
    @Transactional
    public boolean toggleVisible(Long id) {
        Product p = find(id);
        p.setVisible(!p.isVisible());
        return p.isVisible();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getFeaturedProducts(int limit) {
        PageRequest pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return productRepository.findByVisibleTrueAndFeaturedTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLatestProducts(int limit) {
        PageRequest pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return productRepository.findByVisibleTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getPublicProducts(ProductFilter filter) {
        int pageIndex = (filter.getPage() != null && filter.getPage() > 0) ? filter.getPage() - 1 : 0;
        int defaultPageSize = (appProperties != null && appProperties.getPageSize() > 0) ? appProperties.getPageSize() : pageSize;

        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        if (filter.getSort() != null && !filter.getSort().isBlank()) {
            if ("price_asc".equalsIgnoreCase(filter.getSort())) {
                sort = Sort.by(Sort.Direction.ASC, "originalPrice");
            } else if ("price_desc".equalsIgnoreCase(filter.getSort())) {
                sort = Sort.by(Sort.Direction.DESC, "originalPrice");
            } else if ("newest".equalsIgnoreCase(filter.getSort())) {
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }
        }

        PageRequest pageable = PageRequest.of(pageIndex, defaultPageSize, sort);

        String keyword = trimToNull(filter.getKeyword());
        List<Long> categoryIds = filter.getEffectiveCategoryIds();
        if (categoryIds.isEmpty()) {
            categoryIds = null;
        }

        return productRepository.findPublicProducts(
                keyword,
                categoryIds,
                filter.getMinPrice(),
                filter.getMaxPrice(),
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getProductCountByCategory() {
        Map<Long, Long> counts = new HashMap<>();
        List<Object[]> rows = productRepository.countProductsByCategory();
        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    counts.put((Long) row[0], ((Number) row[1]).longValue());
                }
            }
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getPublicProductById(Long id) {
        return productRepository.findByIdAndVisibleTrue(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm hoặc sản phẩm đã bị ẩn."));
    }

    private void apply(Product p, ProductForm form, Setting category, List<String[]> specs) {
        p.setCode(form.getCode().trim());
        p.setName(form.getName().trim());
        p.setCategory(category);
        // Man hinh chi co mot gia: originalPrice la gia ban, bo gia khuyen mai cu
        p.setOriginalPrice(form.getOriginalPrice());
        p.setSalePrice(null);
        p.setStockQuantity(form.getStockQuantity());
        p.setVisible(form.isVisible());
        p.setBriefInfo(form.getBriefInfo().trim());
        p.setDescription(form.getDescription().trim());
        p.getSpecifications().clear();
        int order = 1;
        for (String[] spec : specs) {
            ProductSpecification row = new ProductSpecification();
            row.setProduct(p);
            row.setName(spec[0]);
            row.setValue(spec[1]);
            row.setDisplayOrder(order++);
            p.getSpecifications().add(row);
        }
    }

    private void addGallery(Product p, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        int order = p.getImages().stream()
                .map(ProductImage::getDisplayOrder)
                .filter(o -> o != null)
                .max(Integer::compareTo)
                .orElse(0);
        for (MultipartFile file : files) {
            if (!hasFile(file)) {
                continue;
            }
            ProductImage image = new ProductImage();
            image.setProduct(p);
            image.setImageUrl(fileStorageService.store(file, FOLDER));
            image.setDisplayOrder(++order);
            p.getImages().add(image);
        }
    }

    /** Moi dong "Ten: Gia tri"; dong trong bi bo qua. */
    private List<String[]> parseSpecifications(String text) {
        List<String[]> result = new ArrayList<>();
        if (text != null) {
            for (String raw : text.split("\\R")) {
                String line = raw.trim();
                if (line.isEmpty()) {
                    continue;
                }
                int idx = line.indexOf(':');
                String name = idx > 0 ? line.substring(0, idx).trim() : "";
                String value = idx > 0 ? line.substring(idx + 1).trim() : "";
                if (name.isEmpty() || value.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Thông số kỹ thuật: mỗi dòng có dạng Tên: Giá trị (ví dụ Kết nối: USB-C).");
                }
                if (name.length() > 100 || value.length() > 255) {
                    throw new IllegalArgumentException(
                            "Thông số kỹ thuật: tên tối đa 100 ký tự, giá trị tối đa 255 ký tự.");
                }
                result.add(new String[]{name, value});
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập ít nhất một dòng thông số kỹ thuật.");
        }
        return result;
    }

    private Setting requireCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục.");
        }
        return settingRepository.findByIdAndType(categoryId, SettingType.PRODUCT_CATEGORY)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không hợp lệ."));
    }

    private Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm."));
    }

    private ProductRow toRow(Product p) {
        return ProductRow.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .thumbnail(p.getThumbnail())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .currentPrice(p.getCurrentPrice())
                .stockQuantity(p.getStockQuantity())
                .visible(p.isVisible())
                .build();
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
