package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.ProductFilter;
import com.swd392.onlineshop.dto.request.ProductForm;
import com.swd392.onlineshop.dto.response.ProductRow;
import com.swd392.onlineshop.dto.response.ProductView;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.entity.Setting;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Marketing: View Product List, Add New Product, View Product Details,
 * Edit Product, Show/Hide Product (PR01-PR05).
 * Public: Featured products, latest products, public catalog, product details.
 */
public interface ProductService {

    /** page bat dau tu 1. */
    Page<ProductRow> search(ProductFilter filter, int page);

    /** Danh muc san pham dang active, dung cho bo loc va form. */
    List<Setting> findCategories();

    /** Danh sach san pham cho dropdown (ma + ten). */
    List<ProductRow> findAllForSelect();

    ProductView getView(Long id);

    ProductForm getForm(Long id);

    /** excludeId = null khi tao moi. */
    boolean codeExists(String code, Long excludeId);

    Long create(ProductForm form);

    void update(Long id, ProductForm form);

    /** Doi Hien/An, tra ve trang thai moi. */
    boolean toggleVisible(Long id);

    List<Product> getFeaturedProducts(int limit);

    List<Product> getLatestProducts(int limit);

    Page<Product> getPublicProducts(ProductFilter filter);

    Product getPublicProductById(Long id);
}
