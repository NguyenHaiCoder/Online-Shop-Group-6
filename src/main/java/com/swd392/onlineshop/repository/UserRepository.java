package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.enums.UserStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    @Query("""
            select u from User u
            join fetch u.role r
            where r.value = 'SALE' and u.status = :status
            order by u.fullName
            """)
    List<User> findByRoleValueAndStatus(UserStatus status);
}
