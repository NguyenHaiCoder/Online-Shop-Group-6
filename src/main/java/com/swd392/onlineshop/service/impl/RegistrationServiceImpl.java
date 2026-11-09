package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.request.RegisterForm;
import com.swd392.onlineshop.entity.*;
import com.swd392.onlineshop.enums.*;
import com.swd392.onlineshop.repository.*;
import com.swd392.onlineshop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository users;
    private final SettingRepository settings;
    private final CustomerRepository customers;
    private final PasswordEncoder passwords;
    @Override @Transactional public void register(RegisterForm f) {
        if (!Objects.equals(f.getPassword(), f.getConfirmPassword()))
            throw new FormException("confirmPassword", "Mật khẩu nhập lại không khớp.");
        if (f.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new FormException("password", "Mật khẩu vượt giới hạn 72 byte UTF-8.");
        String email = f.getEmail().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw new FormException("email", "Email đã được sử dụng.");
        // Role is always chosen by the server, never from public form input.
        Setting role = settings.findByTypeAndValue(SettingType.USER_ROLE, "CUSTOMER").filter(Setting::isActive)
                .orElseThrow(() -> new FormException("email", "Chưa cấu hình vai trò khách hàng. Vui lòng liên hệ quản trị viên."));
        User u = new User();
        u.setCode("ND" + UUID.randomUUID().toString().replace("-", "").substring(0,18));
        u.setFullName(f.getFullName().trim()); u.setEmail(email); u.setGender(f.getGender());
        u.setMobile(f.getMobile()); u.setAddress(f.getAddress()); u.setPassword(passwords.encode(f.getPassword()));
        u.setRole(role); u.setStatus(UserStatus.ACTIVE); u.setEmailVerified(false); u.setVisible(true);
        users.saveAndFlush(u);
        String code = "ND" + String.format("%04d", u.getId());
        if (users.existsByCode(code)) throw new FormException("email", "Không thể tạo mã tài khoản. Liên hệ quản trị viên.");
        u.setCode(code);
        Customer customer = customers.findByEmailIgnoreCase(email).orElseGet(() -> {
            Customer c = new Customer(); c.setEmail(email);
            c.setCode("KH" + UUID.randomUUID().toString().replace("-", "").substring(0,18));
            c.setFullName(u.getFullName()); c.setGender(u.getGender()); c.setMobile(u.getMobile()); c.setAddress(u.getAddress());
            return c;
        });
        if (customer.getUser() != null) throw new FormException("email", "Hồ sơ khách hàng đã có tài khoản.");
        boolean creating = customer.getId() == null;
        customer.setUser(u);
        customers.saveAndFlush(customer);
        if (creating) {
            String customerCode = "KH" + String.format("%04d", customer.getId());
            if (customers.existsByCode(customerCode)) throw new FormException("email", "Không thể tạo mã khách hàng.");
            customer.setCode(customerCode);
        }
    }
}