package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.User;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByCode(String code);
    boolean existsByRoleId(Long roleId);
}