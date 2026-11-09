package com.swd392.onlineshop.exception;

/** Khong tim thay ban ghi. Handler tra trang loi 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
