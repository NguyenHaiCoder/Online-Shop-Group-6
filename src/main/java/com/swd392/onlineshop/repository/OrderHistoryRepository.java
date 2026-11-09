package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    @Query("""
            select h from OrderHistory h
            left join fetch h.actor
            where h.order.id = :orderId
            order by h.createdAt asc
            """)
    List<OrderHistory> findByOrderIdOrderByCreatedAtAsc(@Param("orderId") Long orderId);
}
