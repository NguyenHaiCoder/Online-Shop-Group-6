package com.swd392.onlineshop.controller.auth;

import com.swd392.onlineshop.dto.request.RegisterForm;
import com.swd392.onlineshop.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller @RequiredArgsConstructor
public class AuthController {
    private final RegistrationService registration;
    @GetMapping("/auth/login") public String login() { return "auth/login/login"; }
    @GetMapping("/auth/register") public String register(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register/register";
    }
    @PostMapping("/auth/register") public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult errors, Model model, RedirectAttributes flash) {
        if (!errors.hasErrors()) {
            try {
                registration.register(form);
                flash.addFlashAttribute("successMessage", "Đăng ký thành công. Bạn có thể đăng nhập ngay.");
                return "redirect:/auth/login";
            } catch (FormException ex) { errors.rejectValue(ex.getField(), "invalid", ex.getMessage()); }
            catch (DataIntegrityViolationException ex) { errors.rejectValue("email", "duplicate", "Email đã được sử dụng."); }
        }
        Map<String,String> messages = new LinkedHashMap<>();
        errors.getFieldErrors().forEach(e -> messages.putIfAbsent(e.getField(), e.getDefaultMessage()));
        model.addAttribute("registerErrors", messages);
        form.setPassword(null); form.setConfirmPassword(null);
        return "auth/register/register";
    }
}