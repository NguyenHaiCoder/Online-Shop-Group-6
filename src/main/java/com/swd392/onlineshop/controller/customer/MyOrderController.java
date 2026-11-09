package com.swd392.onlineshop.controller.customer;

import com.swd392.onlineshop.entity.Order;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.exception.BusinessException;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/my-orders")
@RequiredArgsConstructor
public class MyOrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping
    public String myOrders(@RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "keyword", required = false) String keyword,
                           Authentication authentication,
                           Model model) {
        Long userId = requireUserId(authentication);
        Page<Order> orderPage = orderService.getCustomerOrders(userId, page, status, keyword);
        model.addAttribute("page", orderPage);
        model.addAttribute("selectedStatus", (status == null || status.isBlank()) ? "ALL" : status.toUpperCase());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "customer/orders/my-orders";
    }

    @GetMapping("/{id}")
    public String orderInformation(@PathVariable("id") Long id,
                                   Authentication authentication,
                                   Model model) {
        Long userId = requireUserId(authentication);
        Order order = orderService.getCustomerOrderDetails(id, userId);
        model.addAttribute("order", order);
        return "customer/orders/order-information";
    }

    @PostMapping("/{id}/update")
    public String updateOrder(@PathVariable("id") Long id,
                              HttpSession session,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Long userId = requireUserId(authentication);
        orderService.prepareUpdateOrder(id, userId, session);
        redirectAttributes.addFlashAttribute("successMessage", "Các sản phẩm của đơn #" + id + " đã được nạp vào giỏ hàng. Bạn có thể sửa thông tin và gửi lại đơn.");
        return "redirect:/cart";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Long userId = requireUserId(authentication);
        orderService.cancelOrder(id, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + id + " thành công.");
        return "redirect:/my-orders/" + id;
    }

    private Long requireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException("Vui lòng đăng nhập để thực hiện chức năng này.");
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin tài khoản."));
    }
}

