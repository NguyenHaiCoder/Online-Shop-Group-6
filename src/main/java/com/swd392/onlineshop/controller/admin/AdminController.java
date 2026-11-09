package com.swd392.onlineshop.controller.admin;

import com.swd392.onlineshop.dto.request.*;
import com.swd392.onlineshop.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.Set;

@Controller @RequiredArgsConstructor @RequestMapping("/admin")
public class AdminController {
    private final UserService users;
    private final SettingService settings;
    private Pageable pageable(int page, String sort, String dir, Set<String> allowed) {
        String field = allowed.contains(sort) ? sort : "id";
        Sort ordering = Sort.by("asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC, field);
        if (!"id".equals(field)) ordering = ordering.and(Sort.by("id"));
        return PageRequest.of(Math.max(0, page - 1), 10, ordering);
    }
    @GetMapping({"", "/"}) public String index() { return "redirect:/admin/users"; }
    @GetMapping("/users") public String users(@ModelAttribute UserFilter filter,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="id") String sort,
            @RequestParam(defaultValue="desc") String dir, Model model) {
        model.addAttribute("page", users.search(filter, pageable(page, sort, dir,
                Set.of("id","code","fullName","email","mobile","gender","role","status","visible","emailVerified"))));
        model.addAttribute("roles", settings.roles());
        return "admin/users/user-list";
    }
    @GetMapping("/users/new") public String newUser(Model model) { return userForm(null, new UserForm(), model); }
    @GetMapping("/users/{id}") public String user(@PathVariable Long id, Model model) {
        model.addAttribute("view", users.view(id));
        return userForm(id, users.form(id), model, false);
    }
    @GetMapping("/users/{id}/edit") public String editUser(@PathVariable Long id, Model model) { return userForm(id, users.form(id), model); }
    private String userForm(Long id, UserForm form, Model model) { return userForm(id, form, model, true); }
    private String userForm(Long id, UserForm form, Model model, boolean editing) {
        model.addAttribute("editing", editing);
        model.addAttribute("form", form); model.addAttribute("creating", id == null);
        model.addAttribute("recordId", id); model.addAttribute("roles", settings.roles());
        return "admin/users/user-details";
    }
    @PostMapping({"/users", "/users/{id}"}) public String saveUser(@PathVariable(required=false) Long id,
            @Valid @ModelAttribute("form") UserForm form, BindingResult errors, Principal actor,
            Model model, RedirectAttributes flash) {
        if (!errors.hasErrors()) {
            try {
                Long saved = users.save(id, form, actor.getName());
                flash.addFlashAttribute("successMessage", "Đã lưu người dùng.");
                return "redirect:/admin/users/" + saved;
            } catch (FormException ex) { errors.rejectValue(ex.getField(), "invalid", ex.getMessage()); }
            catch (DataIntegrityViolationException ex) { errors.reject("duplicate", "Mã hoặc email đã tồn tại. Vui lòng kiểm tra lại."); }
        }
        form.setPassword(null); form.setConfirmPassword(null);
        return userForm(id, form, model);
    }
    @PostMapping("/users/{id}/toggle") public String toggleUser(@PathVariable Long id, RedirectAttributes flash) {
        users.toggle(id); flash.addFlashAttribute("successMessage", "Đã đổi trạng thái hiển thị. Quyền đăng nhập không thay đổi.");
        return "redirect:/admin/users";
    }
    @GetMapping("/settings") public String settings(@ModelAttribute SettingFilter filter,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="id") String sort,
            @RequestParam(defaultValue="desc") String dir, Model model) {
        model.addAttribute("page", settings.search(filter, pageable(page, sort, dir,
                Set.of("id","type","code","name","groupName","value","displayOrder","active"))));
        return "admin/settings/setting-list";
    }
    @GetMapping("/settings/new") public String newSetting(Model model) { return settingForm(null, new SettingForm(), model); }
    @GetMapping("/settings/{id}") public String setting(@PathVariable Long id, Model model) {
        model.addAttribute("view", settings.view(id));
        return settingForm(id, settings.form(id), model, false);
    }
    @GetMapping("/settings/{id}/edit") public String editSetting(@PathVariable Long id, Model model) { return settingForm(id, settings.form(id), model); }
    private String settingForm(Long id, SettingForm form, Model model) { return settingForm(id, form, model, true); }
    private String settingForm(Long id, SettingForm form, Model model, boolean editing) {
        model.addAttribute("editing", editing);
        model.addAttribute("form", form); model.addAttribute("creating", id == null); model.addAttribute("recordId", id);
        model.addAttribute("locked", id != null && form.getType() == com.swd392.onlineshop.enums.SettingType.USER_ROLE);
        return "admin/settings/setting-details";
    }
    @PostMapping({"/settings", "/settings/{id}"}) public String saveSetting(@PathVariable(required=false) Long id,
            @Valid @ModelAttribute("form") SettingForm form, BindingResult errors, Model model, RedirectAttributes flash) {
        if (!errors.hasErrors()) {
            try {
                Long saved = settings.save(id, form); flash.addFlashAttribute("successMessage", "Đã lưu cấu hình.");
                return "redirect:/admin/settings/" + saved;
            } catch (FormException ex) { errors.rejectValue(ex.getField(), "invalid", ex.getMessage()); }
            catch (DataIntegrityViolationException ex) { errors.reject("duplicate", "Mã hoặc giá trị đã tồn tại trong nhóm."); }
        }
        return settingForm(id, form, model);
    }
    @PostMapping("/settings/{id}/toggle") public String toggleSetting(@PathVariable Long id, RedirectAttributes flash) {
        try { settings.toggle(id); flash.addFlashAttribute("successMessage", "Đã đổi trạng thái cấu hình."); }
        catch (FormException ex) { flash.addFlashAttribute("errorMessage", ex.getMessage()); }
        return "redirect:/admin/settings";
    }
}