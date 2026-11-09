package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"user"})
    Optional<Customer> findByUserId(Long userId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByCode(String code);
}
