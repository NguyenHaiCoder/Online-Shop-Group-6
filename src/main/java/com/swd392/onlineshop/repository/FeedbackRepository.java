package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Feedback;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {

    @EntityGraph(attributePaths = {"images", "product", "customer"})
    Optional<Feedback> findById(Long id);

    boolean existsByCode(String code);
}
