package com.swd392.onlineshop.entity;

import com.swd392.onlineshop.enums.TokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Token trong link gui qua email:
 * - VERIFY_EMAIL: User Register -> click link -> kich hoat tai khoan.
 * - RESET_PASSWORD: Reset Password -> click link -> Rescue Update (nhap mat khau moi 2 lan).
 * Thoi han link lay tu config file. Moi token chi dung duoc 1 lan.
 * <p>
 * Phai luu DB (khong dung HttpSession) vi user co the mo link o trinh duyet/thiet bi khac.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_tokens")
public class UserToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Chuoi ngau nhien (UUID) dat trong link */
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenType type;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** null = chua dung */
    private LocalDateTime usedAt;

    public boolean isUsable() {
        return usedAt == null && expiresAt.isAfter(LocalDateTime.now());
    }
}
