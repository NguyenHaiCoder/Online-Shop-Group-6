package com.swd392.onlineshop.exception;

/** Du lieu dung dinh dang nhung nghiep vu khong cho phep. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
