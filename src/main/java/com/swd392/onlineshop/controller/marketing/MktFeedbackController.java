package com.swd392.onlineshop.controller.marketing;

import com.swd392.onlineshop.dto.request.FeedbackFilter;
import com.swd392.onlineshop.dto.request.FeedbackForm;
import com.swd392.onlineshop.service.FeedbackService;
import com.swd392.onlineshop.service.ProductService;
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

import java.security.Principal;

/**
 * MF01 View Feedback List, MF02 Add New Feedback, MF03 View Feedback Details,
 * MF04 Edit Feedback, MF05 Show/Hide Feedback.
 */
@Controller
@RequestMapping("/marketing/feedbacks")
@RequiredArgsConstructor
public class MktFeedbackController {

    private static final String LIST = "marketing/feedbacks/feedback-list";
    private static final String DETAILS = "marketing/feedbacks/feedback-details";
    private static final String FORM = "marketing/feedbacks/feedback-form";

    private final FeedbackService feedbackService;
    private final ProductService productService;

    @GetMapping
    public String list(@ModelAttribute("filter") FeedbackFilter filter,
                       @RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("page", feedbackService.search(filter, page));
        return LIST;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new FeedbackForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") FeedbackForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        checkCustomer(form, result);
        if (result.hasErrors()) {
            return formView(model, null);
        }
        try {
            Long id = feedbackService.create(form);
            redirect.addFlashAttribute("successMessage", "Đã tạo phản hồi.");
            return "redirect:/marketing/feedbacks/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, null);
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("feedback", feedbackService.getView(id));
        return DETAILS;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", feedbackService.getForm(id));
        return formView(model, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") FeedbackForm form,
                         BindingResult result, Principal principal, Model model, RedirectAttributes redirect) {
        checkCustomer(form, result);
        if (result.hasErrors()) {
            return formView(model, id);
        }
        try {
            feedbackService.update(id, form, principal.getName());
            redirect.addFlashAttribute("successMessage", "Đã lưu thay đổi.");
            return "redirect:/marketing/feedbacks/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, id);
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(defaultValue = "list") String from,
                         Principal principal, RedirectAttributes redirect) {
        boolean visible = feedbackService.toggleVisible(id, principal.getName());
        redirect.addFlashAttribute("successMessage", visible ? "Đã hiện phản hồi." : "Đã ẩn phản hồi.");
        return "detail".equals(from) ? "redirect:/marketing/feedbacks/" + id : "redirect:/marketing/feedbacks";
    }

    /** Phan hoi gan voi khach hang co san cung email (feedbacks.customer_id NOT NULL). */
    private void checkCustomer(FeedbackForm form, BindingResult result) {
        if (!result.hasFieldErrors("email") && !feedbackService.customerExists(form.getEmail())) {
            result.rejectValue("email", "unknown",
                    "Email chưa có trong danh sách khách hàng. Hãy thêm khách hàng trước.");
        }
    }

    private String formView(Model model, Long id) {
        model.addAttribute("creating", id == null);
        model.addAttribute("products", productService.findAllForSelect());
        if (id != null) {
            model.addAttribute("feedback", feedbackService.getView(id));
        }
        return FORM;
    }
}
