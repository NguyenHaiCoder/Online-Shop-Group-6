package com.swd392.onlineshop.repository;
import com.swd392.onlineshop.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.*;
public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findByVisibleTrueAndFeaturedTrueOrderByUpdatedAtDescIdDesc(Pageable pageable);
    Page<Post> findByVisibleTrueOrderByUpdatedAtDescIdDesc(Pageable pageable);
    Optional<Post> findByIdAndVisibleTrue(Long id);
}