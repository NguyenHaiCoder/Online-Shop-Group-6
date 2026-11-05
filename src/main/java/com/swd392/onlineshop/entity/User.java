package com.swd392.onlineshop.entity;

import com.swd392.onlineshop.enums.Gender;
import com.swd392.onlineshop.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tai khoan dang nhap cua Customer, Marketing, Sale, Sale Manager, Admin.
 * - Register (pop-up): full name, gender, email, mobile, address, password.
 *   Luu User voi status NOT_VERIFIED + UserToken VERIFY_EMAIL, gui link qua email.
 *   Click link -> ACTIVE, dong thoi tao/lien ket Customer (contact) cung email.
 * - Spring Security form login (session): username = email, password = BCrypt,
 *   authority = "ROLE_" + role.value, chi cho dang nhap khi status = ACTIVE.
 * - User Profile: sua thong tin + avatar, khong duoc sua email.
 * - Admin (User List / User Details): them moi -> sinh mat khau ngau nhien gui qua email;
 *   khi sua chi duoc doi role va status.
 * - Sale ACTIVE duoc tu dong giao don xoay vong (xem Order.assignedSale).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String fullName;

    /** Dung lam username khi dang nhap, khong cho sua */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Mat khau da ma hoa BCrypt */
    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 15)
    private String mobile;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String avatar;

    /** Setting co type = USER_ROLE (EAGER de Spring Security load quyen khi login) */
    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Setting role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.NOT_VERIFIED;
}
