package com.swd392.onlineshop.controller.auth;

import com.swd392.onlineshop.dto.request.ProfileForm;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.enums.Gender;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String profilePage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user != null && !model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", ProfileForm.from(user));
        }
        return "auth/profile/profile";
    }

    @PostMapping
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam(value = "gender", required = false) String genderStr,
                                @RequestParam(value = "mobile", required = false) String mobile,
                                @RequestParam(value = "address", required = false) String address,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                                Principal principal,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Người dùng không tồn tại."));

        Map<String, String> errors = new HashMap<>();
        if (fullName == null || fullName.trim().length() < 2 || fullName.trim().length() > 100) {
            errors.put("fullName", "Họ tên phải từ 2 đến 100 ký tự.");
        }
        if (mobile != null && mobile.trim().length() > 15) {
            errors.put("mobile", "Số điện thoại tối đa 15 ký tự.");
        }
        if (address != null && address.trim().length() > 255) {
            errors.put("address", "Địa chỉ tối đa 255 ký tự.");
        }

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("profileErrors", errors);
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật hồ sơ thất bại, vui lòng kiểm tra lại thông tin.");
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/profile");
        }

        user.setFullName(fullName.trim());
        if (genderStr != null && !genderStr.isBlank()) {
            try {
                user.setGender(Gender.valueOf(genderStr));
            } catch (Exception ignored) {
                user.setGender(null);
            }
        } else {
            user.setGender(null);
        }
        user.setMobile(mobile != null ? mobile.trim() : null);
        user.setAddress(address != null ? address.trim() : null);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            if (avatarFile.getSize() > 2L * 1024 * 1024) {
                errors.put("avatar", "Ảnh đại diện tối đa 2 MB.");
                redirectAttributes.addFlashAttribute("profileErrors", errors);
                redirectAttributes.addFlashAttribute("errorMessage", "Ảnh đại diện tối đa 2 MB.");
                String referer = request.getHeader("Referer");
                return "redirect:" + (referer != null ? referer : "/profile");
            }
            try {
                String avatarUrl = fileStorageService.store(avatarFile, "avatars");
                user.setAvatar(avatarUrl);
            } catch (Exception ex) {
                log.warn("Lỗi upload avatar lên Cloudinary: {}", ex.getMessage());
                errors.put("avatar", "Lỗi tải ảnh: " + ex.getMessage());
                redirectAttributes.addFlashAttribute("profileErrors", errors);
                String referer = request.getHeader("Referer");
                return "redirect:" + (referer != null ? referer : "/profile");
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/profile");
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        return "auth/password/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Người dùng không tồn tại."));

        Map<String, String> errors = new HashMap<>();

        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            errors.put("currentPassword", "Mật khẩu hiện tại không đúng.");
        }
        if (newPassword == null || !newPassword.matches("(?=.*[A-Za-z])(?=.*[0-9]).{6,32}")) {
            errors.put("newPassword", "Mật khẩu mới cần 6–32 ký tự, có ít nhất một chữ và một số.");
        } else if (passwordEncoder.matches(newPassword, user.getPassword())) {
            errors.put("newPassword", "Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        if (!Objects.equals(newPassword, confirmPassword)) {
            errors.put("confirmPassword", "Mật khẩu xác nhận không khớp.");
        }

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("passwordErrors", errors);
            redirectAttributes.addFlashAttribute("errorMessage", "Đổi mật khẩu thất bại, vui lòng kiểm tra lại thông tin.");
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/profile/change-password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
