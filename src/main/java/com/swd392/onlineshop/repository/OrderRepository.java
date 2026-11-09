package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Order;
import com.swd392.onlineshop.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"customer", "assignedSale", "items", "items.product", "items.product.category"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findDetailById(@Param("id") Long id);

    boolean existsByCustomer_Id(Long customerId);

    boolean existsByCustomer_IdAndStatus(Long customerId, OrderStatus status);
}
