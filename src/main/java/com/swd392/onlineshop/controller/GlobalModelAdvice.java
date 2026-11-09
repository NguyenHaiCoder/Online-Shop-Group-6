package com.swd392.onlineshop.controller;

import com.swd392.onlineshop.config.AppProperties;
import com.swd392.onlineshop.dto.response.UserRow;
import com.swd392.onlineshop.entity.Post;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.enums.SettingType;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.CartService;
import com.swd392.onlineshop.service.PostService;
import com.swd392.onlineshop.service.ProductService;
import com.swd392.onlineshop.service.SettingService;
import com.swd392.onlineshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final UserService users;
    private final SettingService settingService;
    private final ProductService productService;
    private final PostService postService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @ModelAttribute("currentUser")
    public UserRow currentUser(Principal principal) {
        return principal == null ? null : users.current(principal.getName());
    }

    @ModelAttribute("profileForm")
    public com.swd392.onlineshop.dto.request.ProfileForm profileForm(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName())
                .map(com.swd392.onlineshop.dto.request.ProfileForm::from)
                .orElse(null);
    }

    @ModelAttribute("productCategories")
    public List<Setting> productCategories() {
        return settingService.getActiveSettingsByType(SettingType.PRODUCT_CATEGORY);
    }

    @ModelAttribute("postCategories")
    public List<Setting> postCategories() {
        return settingService.getActiveSettingsByType(SettingType.POST_CATEGORY);
    }

    @ModelAttribute("latestProducts")
    public List<Product> latestProducts() {
        int limit = appProperties != null && appProperties.getSider().getLatestSize() > 0
                ? appProperties.getSider().getLatestSize() : 5;
        return productService.getLatestProducts(limit);
    }

    @ModelAttribute("latestPosts")
    public List<Post> latestPosts() {
        int limit = appProperties != null && appProperties.getSider().getLatestSize() > 0
                ? appProperties.getSider().getLatestSize() : 5;
        return postService.getLatestPosts(limit);
    }

    @ModelAttribute("shopHotline")
    public String shopHotline() {
        return appProperties != null ? appProperties.getShop().getHotline() : "1900 6868";
    }

    @ModelAttribute("shopEmail")
    public String shopEmail() {
        return appProperties != null ? appProperties.getShop().getEmail() : "support@novashop.vn";
    }

    @ModelAttribute("shopAddress")
    public String shopAddress() {
        return appProperties != null ? appProperties.getShop().getAddress() : "Khu Công Nghệ Cao Hòa Lạc, Hà Nội";
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpServletRequest request, Authentication authentication) {
        HttpSession session = request != null ? request.getSession(false) : null;
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            userId = userRepository.findByEmail(authentication.getName()).map(User::getId).orElse(null);
        }
        return cartService.getCartCount(session, userId);
    }

    @ModelAttribute("cartDrawerLines")
    public List<com.swd392.onlineshop.dto.response.CartLine> cartDrawerLines(HttpServletRequest request, Authentication authentication) {
        HttpSession session = request != null ? request.getSession(false) : null;
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            userId = userRepository.findByEmail(authentication.getName()).map(User::getId).orElse(null);
        }
        return cartService.getCartLines(session, userId);
    }
}
