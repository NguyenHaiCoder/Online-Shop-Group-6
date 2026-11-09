package com.swd392.onlineshop.service;

import com.swd392.onlineshop.entity.Order;

public interface EmailService {

    void sendOrderConfirmation(Order order);
}

