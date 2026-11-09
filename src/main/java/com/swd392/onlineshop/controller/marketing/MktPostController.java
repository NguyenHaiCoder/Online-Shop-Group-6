package com.swd392.onlineshop.controller.marketing;

import com.swd392.onlineshop.dto.request.PostFilter;
import com.swd392.onlineshop.dto.request.PostForm;
import com.swd392.onlineshop.service.PostService;
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
 * MP01 View Post List, MP02 Add New Post, MP03 View Post Details,
 * MP04 Edit Post, MP05 Show/Hide Post.
 */
@Controller
@RequestMapping("/marketing/posts")
@RequiredArgsConstructor
public class MktPostController {

    private static final String LIST = "marketing/posts/post-list";
    private static final String DETAILS = "marketing/posts/post-details";
    private static final String FORM = "marketing/posts/post-form";

    private final PostService postService;

    @GetMapping
    public String list(@ModelAttribute("filter") PostFilter filter,
                       @RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("page", postService.search(filter, page));
        return LIST;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new PostForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PostForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes redirect) {
        if (!hasFile(form.getThumbnailFile())) {
            result.rejectValue("thumbnailFile", "required", "Vui lòng chọn ảnh đại diện.");
        }
        if (result.hasErrors()) {
            return formView(model, null);
        }
        try {
            Long id = postService.create(form, principal.getName());
            redirect.addFlashAttribute("successMessage", "Đã tạo bài viết.");
            return "redirect:/marketing/posts/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, null);
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.getView(id));
        return DETAILS;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", postService.getForm(id));
        return formView(model, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") PostForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formView(model, id);
        }
        try {
            postService.update(id, form);
            redirect.addFlashAttribute("successMessage", "Đã lưu thay đổi.");
            return "redirect:/marketing/posts/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return formView(model, id);
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(defaultValue = "list") String from,
                         RedirectAttributes redirect) {
        boolean visible = postService.toggleVisible(id);
        redirect.addFlashAttribute("successMessage", visible ? "Đã hiện bài viết." : "Đã ẩn bài viết.");
        return "detail".equals(from) ? "redirect:/marketing/posts/" + id : "redirect:/marketing/posts";
    }

    private String formView(Model model, Long id) {
        model.addAttribute("creating", id == null);
        if (id != null) {
            model.addAttribute("post", postService.getView(id));
        }
        return FORM;
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
