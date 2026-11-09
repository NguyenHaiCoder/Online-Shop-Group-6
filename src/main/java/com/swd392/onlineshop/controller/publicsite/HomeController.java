package com.swd392.onlineshop.controller.publicsite;
import com.swd392.onlineshop.dto.request.ProductFilter;
import com.swd392.onlineshop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller @RequiredArgsConstructor
public class HomeController {
    private final ShopService shop;
    @GetMapping("/") public String home(Model model) {
        var home=shop.home(); model.addAttribute("featuredProducts",home.featuredProducts()); model.addAttribute("hotPosts",home.hotPosts());
        model.addAttribute("productCategories",home.categories()); model.addAttribute("sliders",home.sliders());
        return "public/home/home";
    }
    @GetMapping("/products") public String products(@ModelAttribute ProductFilter filter,@RequestParam(defaultValue="1") int page,Model model) {
        model.addAttribute("productCategories",shop.categories()); model.addAttribute("page",shop.products(filter,page)); return "public/products/product-list";
    }
    @GetMapping("/products/{id}") public String product(@PathVariable Long id,Model model) { model.addAttribute("product",shop.product(id)); return "public/products/product-details"; }
    @GetMapping("/blogs") public String posts(@RequestParam(defaultValue="1") int page,Model model) { model.addAttribute("page",shop.posts(page)); return "public/blogs/blog-list"; }
    @GetMapping("/blogs/{id}") public String post(@PathVariable Long id,Model model) { model.addAttribute("post",shop.post(id)); return "public/blogs/blog-details"; }
}