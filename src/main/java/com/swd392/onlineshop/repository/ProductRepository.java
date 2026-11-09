package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    List<Product> findAllByOrderByNameAsc();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByVisibleTrueAndFeaturedTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByVisibleTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "images"})
    Optional<Product> findByIdAndVisibleTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category"})
    @Query(value = """
        SELECT p FROM Product p
        WHERE p.visible = true
          AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.briefInfo) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryIds IS NULL OR p.category.id IN :categoryIds)
          AND (:minPrice IS NULL OR COALESCE(p.salePrice, p.originalPrice) >= :minPrice)
          AND (:maxPrice IS NULL OR COALESCE(p.salePrice, p.originalPrice) <= :maxPrice)
    """, countQuery = """
        SELECT COUNT(p) FROM Product p
        WHERE p.visible = true
          AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.briefInfo) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryIds IS NULL OR p.category.id IN :categoryIds)
          AND (:minPrice IS NULL OR COALESCE(p.salePrice, p.originalPrice) >= :minPrice)
          AND (:maxPrice IS NULL OR COALESCE(p.salePrice, p.originalPrice) <= :maxPrice)
    """)
    Page<Product> findPublicProducts(@Param("keyword") String keyword,
                                     @Param("categoryIds") Collection<Long> categoryIds,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     Pageable pageable);

    @Query("""
        SELECT s.id, COUNT(p.id)
        FROM Setting s
        LEFT JOIN Product p ON p.category.id = s.id AND p.visible = true
        WHERE s.type = com.swd392.onlineshop.enums.SettingType.PRODUCT_CATEGORY AND s.active = true
        GROUP BY s.id
    """)
    List<Object[]> countProductsByCategory();
}
