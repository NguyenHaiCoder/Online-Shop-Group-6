package com.swd392.onlineshop.repository;
import com.swd392.onlineshop.entity.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.*;
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByVisibleTrueAndFeaturedTrueOrderByUpdatedAtDescIdDesc(Pageable pageable);
    Optional<Product> findByIdAndVisibleTrue(Long id);
}