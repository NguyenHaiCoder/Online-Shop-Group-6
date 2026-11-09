package com.swd392.onlineshop.service;

/** Tinh lai customers.status theo don va tai khoan. Khong gui email. */
public interface CustomerService {

    void refreshStatus(Long customerId);
}
