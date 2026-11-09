package com.swd392.onlineshop.controller.marketing;

import com.swd392.onlineshop.dto.request.CustomerFilter;
import com.swd392.onlineshop.dto.request.CustomerForm;
import com.swd392.onlineshop.service.CustomerService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MC01 View Customer List, MC02 Add New Customer, MC03 View Customer Details,
 * MC04 Edit Customer, MC05 Show/Hide Customer.
 */
@Controller
@RequestMapping("/marketing/customers")
@RequiredArgsConstructor
public class MktCustomerController {

    private static final String LIST = "marketing/customers/customer-list";
    private static final String DETAILS = "marketing/customers/customer-details";
    private static final String FORM = "marketing/customers/customer-form";

    private final CustomerService customerService;

    @GetMapping
    public String list(@ModelAttribute("filter") CustomerFilter filter,
                       @RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("page", customerService.search(filter, page));
        return LIST;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new CustomerForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") CustomerForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasFieldErrors("email") && customerService.emailExists(form.getEmail(), null)) {
            result.rejectValue("email", "duplicate", "Email đã thuộc về khách hàng khác.");
        }
        if (result.hasErrors()) {
            return formView(model, null);
        }
        try {
            Long id = customerService.create(form);
            redirect.addFlashAttribute("successMessage", "Đã tạo hồ sơ khách hàng.");
            return "redirect:/marketing/customers/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, null);
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getView(id));
        return DETAILS;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", customerService.getForm(id));
        return formView(model, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") CustomerForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasFieldErrors("email") && customerService.emailExists(form.getEmail(), id)) {
            result.rejectValue("email", "duplicate", "Email đã thuộc về khách hàng khác.");
        }
        if (result.hasErrors()) {
            return formView(model, id);
        }
        try {
            customerService.update(id, form);
            redirect.addFlashAttribute("successMessage", "Đã lưu thay đổi.");
            return "redirect:/marketing/customers/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, id);
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(defaultValue = "list") String from,
                         RedirectAttributes redirect) {
        boolean visible = customerService.toggleVisible(id);
        redirect.addFlashAttribute("successMessage", visible ? "Đã hiện khách hàng." : "Đã ẩn khách hàng.");
        return "detail".equals(from) ? "redirect:/marketing/customers/" + id : "redirect:/marketing/customers";
    }

    private String formView(Model model, Long id) {
        model.addAttribute("creating", id == null);
        if (id != null) {
            model.addAttribute("customer", customerService.getView(id));
        }
        return FORM;
    }
}
