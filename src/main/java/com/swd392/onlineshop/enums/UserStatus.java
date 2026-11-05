package com.swd392.onlineshop.enums;

/**
 * Trang thai tai khoan. Chi ACTIVE moi dang nhap duoc.
 */
public enum UserStatus {
    /** Vua dang ky, chua click link xac thuc trong email */
    NOT_VERIFIED,
    ACTIVE,
    /** Bi Admin khoa */
    INACTIVE
}
