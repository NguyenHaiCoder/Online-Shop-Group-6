package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.config.AppProperties;
import com.swd392.onlineshop.entity.Order;
import com.swd392.onlineshop.entity.OrderItem;
import com.swd392.onlineshop.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final AppProperties appProperties;

    @Override
    public void sendOrderConfirmation(Order order) {
        if (order == null || order.getReceiverEmail() == null || order.getReceiverEmail().isBlank()) {
            return;
        }
        try {
            StringBuilder body = new StringBuilder();
            body.append("Xin chào ").append(order.getReceiverName()).append(",\n\n");
            body.append("Cảm ơn bạn đã đặt hàng tại NOVA SHOP!\n");
            body.append("Mã đơn hàng: ").append(order.getCode() != null ? order.getCode() : order.getId()).append("\n");
            body.append("Tổng thanh toán: ").append(order.getTotalAmount()).append(" ₫\n\n");
            body.append("Danh sách sản phẩm:\n");
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    body.append("- ").append(item.getProductName())
                            .append(" x ").append(item.getQuantity())
                            .append(": ").append(item.getSubtotal()).append(" ₫\n");
                }
            }
            body.append("\nHướng dẫn thanh toán:\n");
            body.append("Ngân hàng: ").append(appProperties.getPayment().getBankName()).append("\n");
            body.append("Số tài khoản: ").append(appProperties.getPayment().getAccountNumber()).append("\n");
            body.append("Chủ tài khoản: ").append(appProperties.getPayment().getAccountName()).append("\n");
            body.append("Nội dung: ").append(appProperties.getPayment().getTransferPrefix())
                    .append(" ").append(order.getCode() != null ? order.getCode() : order.getId()).append("\n\n");
            body.append("Trân trọng,\nNOVA SHOP Team");

            log.info("[EMAIL SIMULATION] Gửi email xác nhận đơn hàng #{} tới {}:\n{}",
                    order.getId(), order.getReceiverEmail(), body);
        } catch (Exception ex) {
            log.error("Không ghi nhận được email xác nhận đơn hàng #{}: {}", order.getId(), ex.getMessage());
        }
    }
}
