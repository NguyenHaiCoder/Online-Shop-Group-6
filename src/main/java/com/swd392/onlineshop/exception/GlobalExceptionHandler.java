package com.swd392.onlineshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(NotFoundException ex, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "Không tìm thấy");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden(ForbiddenException ex, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("error", "Không có quyền");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(OutOfStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String outOfStock(OutOfStockException ex, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("error", "Sản phẩm tạm thời không đủ hàng");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("productName", ex.getProductName());
        return "error";
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String business(BusinessException ex, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("error", "Không thực hiện được");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
