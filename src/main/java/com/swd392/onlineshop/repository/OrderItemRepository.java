package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select i from OrderItem i where i.order.id in :orderIds order by i.id")
    List<OrderItem> findByOrderIdIn(@Param("orderIds") Collection<Long> orderIds);
}
