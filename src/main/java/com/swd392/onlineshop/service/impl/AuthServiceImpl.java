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



@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay tai khoan"));
        boolean active = user.getStatus() == UserStatus.ACTIVE
                && user.getRole() != null && user.getRole().isActive()
                && user.getRole().getType() == com.swd392.onlineshop.enums.SettingType.USER_ROLE
                && SettingServiceImpl.ROLE_VALUES.contains(user.getRole().getValue());
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(active ? "ROLE_" + user.getRole().getValue() : "ROLE_DISABLED")
                .disabled(!active)
                .build();
    }

}
