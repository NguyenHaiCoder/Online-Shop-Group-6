package com.swd392.onlineshop.config;

import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Sau khi login: gop gio hang va dieu huong theo role.
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    private final CartService cartService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Gop gio hang session vao DB
        HttpSession session = request.getSession(false);
        if (session != null && authentication != null) {
            userRepository.findByEmail(authentication.getName()).map(User::getId)
                    .ifPresent(userId -> cartService.mergeSessionCartToDb(session, userId));
        }

        String target = "/";
        if (hasRole(authentication, "ADMIN")) {
            target = "/admin/users";
        } else if (hasRole(authentication, "MARKETING")) {
            target = "/marketing/products";
        } else if (hasRole(authentication, "SALE") || hasRole(authentication, "SALE_MANAGER")) {
            target = "/sales/orders";
        } else {
            SavedRequest saved = requestCache.getRequest(request, response);
            if (saved != null && saved.getRedirectUrl() != null && !saved.getRedirectUrl().isBlank()) {
                target = saved.getRedirectUrl();
            }
        }
        requestCache.removeRequest(request, response);
        if (target.startsWith("http://") || target.startsWith("https://")) {
            response.sendRedirect(target);
        } else {
            response.sendRedirect(request.getContextPath() + target);
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        String authority = "ROLE_" + role;
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            if (authority.equals(granted.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
