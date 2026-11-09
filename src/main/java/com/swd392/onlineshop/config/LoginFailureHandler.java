package com.swd392.onlineshop.config;
import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;
import java.io.IOException;
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    @Override public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        FlashMap flash = new FlashMap();
        flash.put("loginError", "Email hoặc mật khẩu không đúng, hoặc tài khoản chưa được kích hoạt.");
        flash.setTargetRequestPath(request.getContextPath() + "/auth/login");
        new SessionFlashMapManager().saveOutputFlashMap(flash, request, response);
        response.sendRedirect(request.getContextPath() + "/auth/login");
    }
}