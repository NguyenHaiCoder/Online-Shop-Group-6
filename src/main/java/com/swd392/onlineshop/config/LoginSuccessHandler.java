package com.swd392.onlineshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Sau khi login: dieu huong theo role.
 * Gop gio session vao gio DB se noi vao day khi CartService co.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String target = "/";
        if (hasRole(authentication, "ADMIN")) {
            target = "/admin/dashboard";
        } else if (hasRole(authentication, "MARKETING")) {
            target = "/marketing/dashboard";
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
