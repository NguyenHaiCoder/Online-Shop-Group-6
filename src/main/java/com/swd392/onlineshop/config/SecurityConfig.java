package com.swd392.onlineshop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final com.swd392.onlineshop.service.AuthService authService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(new SessionRoleFilter(authService), org.springframework.security.web.context.SecurityContextHolderFilter.class)
                .csrf(csrf -> csrf
                        // Tao CSRF token (va session) ngay dau request. Neu de mac dinh (deferred), token chi duoc
                        // tao khi Thymeleaf render toi form o cuoi trang dai (modal dang nhap), luc do response da
                        // gui di mot phan nen khong tao duoc session -> trang bi cat ngang, mat ui.js va chatbox.
                        .csrfTokenRequestHandler(eagerCsrfTokenHandler())
                        // Chatbox chi doc du lieu cong khai, khong doi trang thai nguoi dung nen khong can CSRF token.
                        .ignoringRequestMatchers("/api/chat"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/blogs/**",
                                "/products/**",
                                "/cart/**",
                                "/feedback/**",
                                "/auth/**",
                                "/api/chat",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/profile", "/profile/**").authenticated()
                        .requestMatchers("/my-orders/**").hasRole("CUSTOMER")
                        .requestMatchers("/marketing/**").hasRole("MARKETING")
                        .requestMatchers("/sales/**").hasAnyRole("SALE", "SALE_MANAGER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }

    private static org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler eagerCsrfTokenHandler() {
        org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler handler =
                new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName(null);
        return handler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
