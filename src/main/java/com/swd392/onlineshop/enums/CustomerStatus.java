package com.swd392.onlineshop.enums;

/**
 * Trang thai contact, he thong tu tinh lai (Marketing khong sua duoc):
 * - CONTACT: chi co thong tin lien he (Marketing them tay, gui feedback,
 *   hoac la nguoi nhan trong don cua nguoi khac)
 * - POTENTIAL: da dang ky tai khoan, hoac da dat don nhung chua co don SUCCESS
 * - CUSTOMER: co it nhat 1 don SUCCESS
 */
public enum CustomerStatus {
    CONTACT,
    POTENTIAL,
    CUSTOMER
}
