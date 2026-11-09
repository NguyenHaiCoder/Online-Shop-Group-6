package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.request.*;
import com.swd392.onlineshop.dto.response.UserRow;
import com.swd392.onlineshop.entity.*;
import com.swd392.onlineshop.enums.*;
import com.swd392.onlineshop.repository.*;
import com.swd392.onlineshop.service.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    private final SettingRepository settings;
    private final PasswordEncoder passwords;
    private final FileStorageService storage;
    @Override public Page<UserRow> search(UserFilter f, Pageable pageable) {
        return users.findAll((root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.getRoleId() != null) p.add(cb.equal(root.get("role").get("id"), f.getRoleId()));
            if (f.getGender() != null) p.add(cb.equal(root.get("gender"), f.getGender()));
            if (f.getStatus() != null) p.add(cb.equal(root.get("status"), f.getStatus()));
            if (f.getVisible() != null) p.add(cb.equal(root.get("visible"), f.getVisible()));
            if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
                String k = "%" + f.getKeyword().trim().toLowerCase(Locale.ROOT) + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("code")), k), cb.like(cb.lower(root.get("fullName")), k),
                        cb.like(cb.lower(root.get("email")), k), cb.like(root.get("mobile"), k)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        }, pageable).map(UserRow::from);
    }
    private User get(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    @Override public com.swd392.onlineshop.dto.response.UserView view(Long id) { return com.swd392.onlineshop.dto.response.UserView.from(get(id)); }
    @Override public UserRow current(String email) { return UserRow.from(users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))); }
    @Override public UserForm form(Long id) {
        User u = get(id); UserForm f = new UserForm();
        f.setCode(u.getCode()); f.setFullName(u.getFullName()); f.setEmail(u.getEmail()); f.setMobile(u.getMobile());
        f.setAddress(u.getAddress()); f.setGender(u.getGender()); f.setRoleId(u.getRole().getId());
        f.setStatus(u.getStatus()); f.setVisible(u.isVisible()); f.setEmailVerified(u.isEmailVerified());
        return f;
    }
    @Override @Transactional public Long save(Long id, UserForm f, String actor) {
        User u = id == null ? new User() : get(id);
        Setting role = settings.findById(f.getRoleId()).filter(s -> s.getType() == SettingType.USER_ROLE && s.isActive()
                && SettingServiceImpl.ROLE_VALUES.contains(s.getValue()))
                .orElseThrow(() -> new FormException("roleId", "Vai trò không hợp lệ hoặc đã ẩn."));
        if (id != null && u.getEmail().equalsIgnoreCase(actor)
                && (!u.getRole().getId().equals(role.getId()) || u.getStatus() != f.getStatus()))
            throw new FormException("roleId", "Không được tự đổi quyền hoặc trạng thái đăng nhập của mình.");
        if (id == null) {
            String email = f.getEmail().trim().toLowerCase(Locale.ROOT);
            if (users.existsByEmailIgnoreCase(email)) throw new FormException("email", "Email đã được sử dụng.");
            if (f.getPassword() == null || !f.getPassword().matches("(?=.*[A-Za-z])(?=.*[0-9]).{6,32}"))
                throw new FormException("password", "Mật khẩu cần 6–32 ký tự, có chữ và số.");
            if (!Objects.equals(f.getPassword(), f.getConfirmPassword())) throw new FormException("confirmPassword", "Mật khẩu nhập lại không khớp.");
            if (f.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
                throw new FormException("password", "Mật khẩu vượt giới hạn 72 byte UTF-8.");
            u.setEmail(email); u.setPassword(passwords.encode(f.getPassword()));
            // Temporary unique code until the identity ID is available.
            u.setCode("ND" + UUID.randomUUID().toString().replace("-", "").substring(0,18));
        }
        u.setFullName(f.getFullName().trim()); u.setMobile(f.getMobile()); u.setAddress(f.getAddress());
        u.setGender(f.getGender()); u.setRole(role); u.setStatus(f.getStatus());
        u.setVisible(f.isVisible()); u.setEmailVerified(f.isEmailVerified());
        if (f.getAvatarFile() != null && !f.getAvatarFile().isEmpty()) {
            if (f.getAvatarFile().getSize() > 2L * 1024 * 1024) throw new FormException("avatarFile", "Ảnh đại diện tối đa 2 MB.");
            try { u.setAvatar(storage.store(f.getAvatarFile(), "avatars")); }
            catch (IllegalArgumentException | IllegalStateException ex) { throw new FormException("avatarFile", ex.getMessage()); }
        }
        users.saveAndFlush(u);
        if (id == null) {
            String code = "ND" + String.format("%04d", u.getId());
            if (users.existsByCode(code)) throw new FormException("code", "Mã tự động bị trùng với dữ liệu cũ.");
            u.setCode(code);
        }
        return u.getId();
    }
    @Override @Transactional public void toggle(Long id) { User u = get(id); u.setVisible(!u.isVisible()); }
}