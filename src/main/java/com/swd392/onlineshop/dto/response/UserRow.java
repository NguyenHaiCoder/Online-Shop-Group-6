package com.swd392.onlineshop.dto.response;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.enums.*;
public record UserRow(Long id, String code, String fullName, String email, String mobile, Gender gender,
                      String avatar, SettingRow role, UserStatus status, boolean emailVerified, boolean visible, java.time.LocalDateTime createdAt) {
    public static UserRow from(User u) {
        return new UserRow(u.getId(), u.getCode(), u.getFullName(), u.getEmail(), u.getMobile(),
                u.getGender(), u.getAvatar(), SettingRow.from(u.getRole()), u.getStatus(), u.isEmailVerified(), u.isVisible(), u.getCreatedAt());
    }
}