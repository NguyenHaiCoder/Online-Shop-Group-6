package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Order;
import com.swd392.onlineshop.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"customer", "assignedSale", "items", "items.product", "items.product.category"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findDetailById(@Param("id") Long id);

    boolean existsByCustomer_Id(Long customerId);

    boolean existsByCustomer_IdAndStatus(Long customerId, OrderStatus status);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "customer"})
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    long countByCustomerIdAndStatus(Long customerId, OrderStatus status);

    long countByCustomerId(Long customerId);

    boolean existsByCode(String code);

    /**
     * Lay danh sach sale active sap xep theo thoi diem nhan don gan nhat tang dan (nguoi chua co don len dau).
     */
    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.role.value = 'SALE' AND u.status = com.swd392.onlineshop.enums.UserStatus.ACTIVE
        GROUP BY u.id
        ORDER BY MAX(COALESCE((SELECT o.createdAt FROM Order o WHERE o.assignedSale.id = u.id), '1970-01-01')) ASC, u.id ASC
    """)
    List<Long> findRoundRobinSaleCandidates();
}
