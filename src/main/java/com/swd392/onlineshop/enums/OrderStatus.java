package com.swd392.onlineshop.enums;

/**
 * Luong trang thai: SUBMITTED -> CONFIRMED -> SHIPPING -> SUCCESS, hoac CANCELLED.
 */
public enum OrderStatus {
    /** Vua dat (Cart Completion). Customer chi duoc Update/Cancel khi don o trang thai nay */
    SUBMITTED,
    CONFIRMED,
    SHIPPING,
    /** Giao thanh cong -> gui email huong dan feedback tung san pham, contact thanh CUSTOMER */
    SUCCESS,
    CANCELLED
}
