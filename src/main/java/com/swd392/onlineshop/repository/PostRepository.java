package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    @Override
    @EntityGraph(attributePaths = "author")
    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "author"})
    List<Post> findByVisibleTrueAndFeaturedTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "author"})
    List<Post> findByVisibleTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "author"})
    Optional<Post> findByIdAndVisibleTrue(Long id);

    @EntityGraph(attributePaths = {"category", "author"})
    @Query("""
        SELECT p FROM Post p
        WHERE p.visible = true
          AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
        ORDER BY p.updatedAt DESC
    """)
    Page<Post> findPublicPosts(@Param("keyword") String keyword,
                               @Param("categoryId") Long categoryId,
                               Pageable pageable);
}
