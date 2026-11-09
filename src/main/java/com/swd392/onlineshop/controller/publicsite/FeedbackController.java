package com.swd392.onlineshop.controller.publicsite;

import com.swd392.onlineshop.dto.request.FeedbackForm;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.repository.ProductRepository;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String feedbackForm(@RequestParam(value = "productId", required = false) Long productId,
                               Authentication authentication,
                               Model model) {
        FeedbackForm form = new FeedbackForm();
        if (productId != null) {
            form.setProductId(productId);
            productRepository.findByIdAndVisibleTrue(productId).ifPresent(p -> model.addAttribute("product", p));
        }

        Long userId = getUserId(authentication);
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                form.setFullName(user.getFullName());
                form.setEmail(user.getEmail());
                form.setMobile(user.getMobile());
                form.setGender(user.getGender());
            });
        }

        model.addAttribute("form", form);
        return "public/feedback/feedback";
    }

    @PostMapping
    public String submitFeedback(@Valid @ModelAttribute("form") FeedbackForm form,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            if (form.getProductId() != null) {
                productRepository.findByIdAndVisibleTrue(form.getProductId())
                        .ifPresent(p -> model.addAttribute("product", p));
            }
            return "public/feedback/feedback";
        }

        Long userId = getUserId(authentication);
        feedbackService.submitFeedback(form, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi phản hồi! Ý kiến của bạn đã được ghi nhận.");
        return "redirect:/";
    }

    private Long getUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return userRepository.findByEmail(authentication.getName()).map(User::getId).orElse(null);
        }
        return null;
    }
}

