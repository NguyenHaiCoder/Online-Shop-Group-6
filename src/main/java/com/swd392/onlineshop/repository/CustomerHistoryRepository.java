package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.CustomerHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerHistoryRepository extends JpaRepository<CustomerHistory, Long> {

    List<CustomerHistory> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}

