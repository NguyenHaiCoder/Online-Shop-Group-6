package com.swd392.onlineshop.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * San pham.
 * - Public: Products List (sap xep theo updated date, nut Buy & Feedback), Product Details,
 *   sider "latest products".
 * - Home Page: featured products.
 * - Marketing: Products List / Product Details (add, view, edit, show/hide).
 * - Cart Completion: tru stockQuantity; Cart Contact bao loi neu het hang.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    /** Ma hien tren man hinh, vi du KB001. Duy nhat. */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String thumbnail;

    @Column(length = 500)
    private String briefInfo;

    @Lob
    private String description;

    /** Setting co type = PRODUCT_CATEGORY */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Setting category;

    /** List price */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal originalPrice;

    /** Gia khuyen mai, null = khong giam gia */
    @Column(precision = 15, scale = 2)
    private BigDecimal salePrice;

    /** So luong con ban duoc */
    @Column(nullable = false)
    private int stockQuantity = 0;

    @Column(nullable = false)
    private boolean featured = false;

    /** Show/Hide Product: chi san pham visible moi hien cho Guest/Customer */
    @Column(nullable = false)
    private boolean visible = true;

    /** Attached images (ngoai thumbnail) */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    /** Tung dong thong so: Kieu ket noi / USB-C. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductSpecification> specifications = new ArrayList<>();

    /** Gia ban theo UI: originalPrice; salePrice chi la cot legacy */
    public BigDecimal getCurrentPrice() {
        return originalPrice;
    }
}
