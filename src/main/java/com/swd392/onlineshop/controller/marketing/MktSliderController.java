package com.swd392.onlineshop.controller.marketing;

import com.swd392.onlineshop.dto.request.SliderFilter;
import com.swd392.onlineshop.dto.request.SliderForm;
import com.swd392.onlineshop.service.SliderService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * MS01 View Slider List, MS02 Add New Slider, MS03 View Slider Details,
 * MS04 Edit Slider, MS05 Show/Hide Slider.
 */
@Controller
@RequestMapping("/marketing/sliders")
@RequiredArgsConstructor
public class MktSliderController {

    private static final String LIST = "marketing/sliders/slider-list";
    private static final String DETAILS = "marketing/sliders/slider-details";
    private static final String FORM = "marketing/sliders/slider-form";

    private final SliderService sliderService;

    @GetMapping
    public String list(@ModelAttribute("filter") SliderFilter filter,
                       @RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("page", sliderService.search(filter, page));
        return LIST;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new SliderForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") SliderForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes redirect) {
        if (!hasFile(form.getImageFile())) {
            result.rejectValue("imageFile", "required", "Vui lòng chọn ảnh banner.");
        }
        if (result.hasErrors()) {
            return formView(model, null);
        }
        try {
            Long id = sliderService.create(form, principal.getName());
            redirect.addFlashAttribute("successMessage", "Đã tạo slider.");
            return "redirect:/marketing/sliders/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, null);
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("slider", sliderService.getView(id));
        return DETAILS;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", sliderService.getForm(id));
        return formView(model, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") SliderForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formView(model, id);
        }
        try {
            sliderService.update(id, form);
            redirect.addFlashAttribute("successMessage", "Đã lưu thay đổi.");
            return "redirect:/marketing/sliders/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, id);
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(defaultValue = "list") String from,
                         RedirectAttributes redirect) {
        boolean visible = sliderService.toggleVisible(id);
        redirect.addFlashAttribute("successMessage", visible ? "Đã hiện slider." : "Đã ẩn slider.");
        return "detail".equals(from) ? "redirect:/marketing/sliders/" + id : "redirect:/marketing/sliders";
    }

    private String formView(Model model, Long id) {
        model.addAttribute("creating", id == null);
        if (id != null) {
            model.addAttribute("slider", sliderService.getView(id));
        }
        return FORM;
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
