package com.swd392.onlineshop.config;

import com.swd392.onlineshop.enums.UserStatus;
import com.swd392.onlineshop.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("email");
        String message = "Email hoặc mật khẩu không đúng.";
        if (email != null && !email.isBlank()) {
            UserStatus status = authService.findStatusByEmail(email.trim()).orElse(null);
            if (status == UserStatus.NOT_VERIFIED) {
                message = "Tài khoản chưa được xác thực. Vui lòng kiểm tra email.";
            } else if (status == UserStatus.INACTIVE) {
                message = "Tài khoản đã bị khóa.";
            }
        }
        FlashMap flashMap = new FlashMap();
        flashMap.put("loginError", message);
        new SessionFlashMapManager().saveOutputFlashMap(flashMap, request, response);
        response.sendRedirect(request.getContextPath() + "/auth/login");
    }
}
