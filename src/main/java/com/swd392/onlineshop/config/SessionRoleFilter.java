package com.swd392.onlineshop.config;

import com.swd392.onlineshop.service.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Apply changes to roles/account status to existing sessions on the next request. */
public class SessionRoleFilter extends OncePerRequestFilter {
    private final AuthService auth;
    public SessionRoleFilter(AuthService auth) { this.auth = auth; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null && current.isAuthenticated() && !(current instanceof AnonymousAuthenticationToken)) {
            try {
                UserDetails user = auth.loadUserByUsername(current.getName());
                if (!user.isEnabled()) throw new DisabledException("Account disabled");
                var refreshed = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
                refreshed.setDetails(current.getDetails());
                SecurityContextHolder.getContext().setAuthentication(refreshed);
            } catch (AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
            }
        }
        chain.doFilter(request, response);
    }
}