package com.swd392.onlineshop.controller;
import com.swd392.onlineshop.dto.response.UserRow;
import com.swd392.onlineshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
@ControllerAdvice @RequiredArgsConstructor
public class GlobalModelAdvice {
    private final UserService users;
    @ModelAttribute("currentUser") public UserRow currentUser(Principal principal) {
        return principal == null ? null : users.current(principal.getName());
    }
}