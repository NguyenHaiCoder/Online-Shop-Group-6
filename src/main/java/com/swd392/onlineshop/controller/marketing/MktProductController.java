package com.swd392.onlineshop.controller.marketing;

import com.swd392.onlineshop.dto.request.ProductFilter;
import com.swd392.onlineshop.dto.request.ProductForm;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * PR01 View Product List, PR02 Add New Product, PR03 View Product Details,
 * PR04 Edit Product, PR05 Show/Hide Product.
 */
@Controller
@RequestMapping("/marketing/products")
@RequiredArgsConstructor
public class MktProductController {

    private static final String LIST = "marketing/products/product-list";
    private static final String DETAILS = "marketing/products/product-details";
    private static final String FORM = "marketing/products/product-form";

    private final ProductService productService;

    @GetMapping
    public String list(@ModelAttribute("filter") ProductFilter filter,
                       @RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("page", productService.search(filter, page));
        model.addAttribute("categories", productService.findCategories());
        return LIST;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ProductForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ProductForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!hasFile(form.getThumbnailFile())) {
            result.rejectValue("thumbnailFile", "required", "Vui lòng chọn ảnh đại diện.");
        }
        if (!result.hasFieldErrors("code") && productService.codeExists(form.getCode(), null)) {
            result.rejectValue("code", "duplicate", "Mã sản phẩm đã tồn tại.");
        }
        if (result.hasErrors()) {
            return formView(model, null);
        }
        try {
            Long id = productService.create(form);
            redirect.addFlashAttribute("successMessage", "Đã tạo sản phẩm.");
            return "redirect:/marketing/products/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, null);
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getView(id));
        return DETAILS;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", productService.getForm(id));
        return formView(model, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ProductForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasFieldErrors("code") && productService.codeExists(form.getCode(), id)) {
            result.rejectValue("code", "duplicate", "Mã sản phẩm đã tồn tại.");
        }
        if (result.hasErrors()) {
            return formView(model, id);
        }
        try {
            productService.update(id, form);
            redirect.addFlashAttribute("successMessage", "Đã lưu thay đổi.");
            return "redirect:/marketing/products/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, id);
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(defaultValue = "list") String from,
                         RedirectAttributes redirect) {
        boolean visible = productService.toggleVisible(id);
        redirect.addFlashAttribute("successMessage", visible ? "Đã hiện sản phẩm." : "Đã ẩn sản phẩm.");
        return "detail".equals(from) ? "redirect:/marketing/products/" + id : "redirect:/marketing/products";
    }

    private String formView(Model model, Long id) {
        model.addAttribute("creating", id == null);
        model.addAttribute("categories", productService.findCategories());
        if (id != null) {
            model.addAttribute("product", productService.getView(id));
        }
        return FORM;
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
