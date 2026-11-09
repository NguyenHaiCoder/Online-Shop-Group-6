package com.swd392.onlineshop.service;

import com.swd392.onlineshop.enums.UserStatus;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AuthService extends UserDetailsService {

    Optional<UserStatus> findStatusByEmail(String email);
}
