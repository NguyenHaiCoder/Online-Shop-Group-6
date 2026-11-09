package com.swd392.onlineshop.exception;

/** Dung tai khoan nhung khong dung pham vi du lieu. Handler tra trang loi 403. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
