package com.swd392.onlineshop.controller.publicsite;

import com.swd392.onlineshop.config.AppProperties;
import com.swd392.onlineshop.dto.request.ReceiverForm;
import com.swd392.onlineshop.dto.response.CartLine;
import com.swd392.onlineshop.entity.Order;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.exception.BusinessException;
import com.swd392.onlineshop.exception.OutOfStockException;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.CartService;
import com.swd392.onlineshop.service.OrderService;
import com.swd392.onlineshop.service.impl.OrderServiceImpl;
import jakarta.servlet.http.HttpSession;
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

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @GetMapping
    public String viewCart(HttpSession session, Authentication authentication, Model model) {
        Long userId = getUserId(authentication);
        List<CartLine> lines = cartService.getCartLines(session, userId);
        model.addAttribute("lines", lines);
        return "public/cart/cart-details";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            @RequestParam(value = "redirect", required = false) String redirect,
                            HttpSession session,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        Long userId = getUserId(authentication);
        cartService.addToCart(session, userId, productId, quantity);
        redirectAttributes.addFlashAttribute("addedProductId", productId);
        redirectAttributes.addFlashAttribute("addedQuantity", quantity);
        redirectAttributes.addFlashAttribute("openDrawer", true);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng.");
        if ("details".equalsIgnoreCase(redirect)) {
            return "redirect:/products/" + productId;
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session,
                                 Authentication authentication) {
        Long userId = getUserId(authentication);
        cartService.updateQuantity(session, userId, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("productId") Long productId,
                                 HttpSession session,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        Long userId = getUserId(authentication);
        cartService.removeFromCart(session, userId, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/cancel-edit")
    public String cancelEdit(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(OrderServiceImpl.SESSION_EDITING_ORDER_ID);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy thao tác chỉnh sửa đơn hàng.");
        return "redirect:/cart";
    }

    @GetMapping("/contact")
    public String cartContact(HttpSession session, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        Long userId = getUserId(authentication);
        List<CartLine> lines = cartService.getCartLines(session, userId);
        if (lines.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            return "redirect:/cart";
        }

        boolean hasWarning = lines.stream().anyMatch(l -> l.getWarning() != null);
        if (hasWarning) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng có sản phẩm chưa đủ điều kiện thanh toán. Vui lòng kiểm tra lại số lượng hoặc bỏ sản phẩm.");
            return "redirect:/cart";
        }

        ReceiverForm form = new ReceiverForm();
        Object editingOrderId = session.getAttribute(OrderServiceImpl.SESSION_EDITING_ORDER_ID);

        if (editingOrderId != null) {
            try {
                Order order = orderService.getOrderById(Long.valueOf(editingOrderId.toString()));
                form.setFullName(order.getReceiverName());
                form.setGender(order.getReceiverGender());
                form.setEmail(order.getReceiverEmail());
                form.setMobile(order.getReceiverMobile());
                form.setAddress(order.getReceiverAddress());
                form.setNote(order.getNote());
            } catch (Exception ignored) {
            }
        } else if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                form.setFullName(user.getFullName());
                form.setGender(user.getGender());
                form.setEmail(user.getEmail());
                form.setMobile(user.getMobile());
                form.setAddress(user.getAddress());
            });
        }

        BigDecimal subtotal = lines.stream().map(CartLine::getLineTotal).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal shippingFee = java.math.BigDecimal.valueOf(30000);
        java.math.BigDecimal totalAmount = subtotal.add(shippingFee);

        model.addAttribute("form", form);
        model.addAttribute("lines", lines);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("totalAmount", totalAmount);
        return "public/cart/cart-contact";
    }

    @PostMapping("/submit")
    public String submitOrder(@Valid @ModelAttribute("form") ReceiverForm form,
                              BindingResult bindingResult,
                              HttpSession session,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Long userId = getUserId(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("lines", cartService.getCartLines(session, userId));
            return "public/cart/cart-contact";
        }

        try {
            orderService.placeOrder(form, session, userId);
            return "redirect:/cart/completion";
        } catch (OutOfStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/completion")
    public String cartCompletion(HttpSession session, Model model) {
        Object lastOrderId = session.getAttribute(OrderServiceImpl.SESSION_LAST_ORDER_ID);
        if (lastOrderId != null) {
            try {
                Order order = orderService.getOrderById(Long.valueOf(lastOrderId.toString()));
                model.addAttribute("order", order);
                model.addAttribute("bankName", appProperties.getPayment().getBankName());
                model.addAttribute("accountNumber", appProperties.getPayment().getAccountNumber());
                model.addAttribute("accountName", appProperties.getPayment().getAccountName());
                model.addAttribute("transferContent", appProperties.getPayment().getTransferPrefix() + " " + (order.getCode() != null ? order.getCode() : order.getId()));
            } catch (Exception ignored) {
            }
        }
        return "public/cart/cart-completion";
    }

    private Long getUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return userRepository.findByEmail(authentication.getName()).map(User::getId).orElse(null);
        }
        return null;
    }
}

