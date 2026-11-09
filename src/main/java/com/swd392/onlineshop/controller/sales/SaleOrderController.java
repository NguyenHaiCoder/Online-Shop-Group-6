package com.swd392.onlineshop.controller.sales;

import com.swd392.onlineshop.dto.request.AssignOrderForm;
import com.swd392.onlineshop.dto.request.OrderFilter;
import com.swd392.onlineshop.dto.request.OrderUpdateForm;
import com.swd392.onlineshop.exception.BusinessException;
import com.swd392.onlineshop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleOrderController {

    private final OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/sales/orders";
    }

    @GetMapping("/orders")
    public String list(@ModelAttribute("filter") OrderFilter filter, Model model) {
        model.addAttribute("page", orderService.list(filter));
        model.addAttribute("sales", orderService.activeSales());
        model.addAttribute("listTitle", orderService.viewingAll() ? "Tất cả đơn hàng" : "Đơn được giao");
        return "sales/orders/order-list";
    }

    @GetMapping("/orders/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getDetail(id));
        model.addAttribute("sales", orderService.activeSales());
        return "sales/orders/order-details";
    }

    @PostMapping("/orders/{id}")
    public String update(@PathVariable Long id,
                         @Valid OrderUpdateForm form,
                         BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            redirect.addFlashAttribute("errorMessage", fieldError(errors, "status", "saleNote"));
            return "redirect:/sales/orders/" + id;
        }
        try {
            orderService.update(id, form);
            redirect.addFlashAttribute("successMessage", "Đã cập nhật đơn.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/sales/orders/" + id;
    }

    @PostMapping("/orders/{id}/assign")
    public String assign(@PathVariable Long id,
                         @Valid AssignOrderForm form,
                         BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            redirect.addFlashAttribute("errorMessage", fieldError(errors, "saleId"));
            return "redirect:/sales/orders/" + id;
        }
        try {
            orderService.assign(id, form);
            redirect.addFlashAttribute("successMessage", "Đã giao đơn.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/sales/orders/" + id;
    }

    private String fieldError(BindingResult errors, String... fields) {
        for (String field : fields) {
            if (!errors.hasFieldErrors(field)) {
                continue;
            }
            var error = errors.getFieldError(field);
            if (error != null && error.isBindingFailure()) {
                return "status".equals(field) ? "Chọn trạng thái hợp lệ." : "Chọn Sale.";
            }
            return error == null ? "Dữ liệu không hợp lệ." : error.getDefaultMessage();
        }
        return "Dữ liệu không hợp lệ.";
    }
}
