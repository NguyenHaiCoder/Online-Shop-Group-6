package com.swd392.onlineshop.exception;

import lombok.Getter;

@Getter
public class OutOfStockException extends RuntimeException {

    private final String productName;

    public OutOfStockException(String productName) {
        super("Sản phẩm " + productName + " không đủ số lượng trong kho hoặc đã ngừng bán.");
        this.productName = productName;
    }
}

