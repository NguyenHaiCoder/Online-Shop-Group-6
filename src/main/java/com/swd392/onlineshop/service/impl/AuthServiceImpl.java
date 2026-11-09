package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.enums.UserStatus;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay tai khoan"));
        boolean active = user.getStatus() == UserStatus.ACTIVE;
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().getValue())
                .disabled(!active)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserStatus> findStatusByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getStatus);
    }
}
