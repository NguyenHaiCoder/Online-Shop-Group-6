package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
