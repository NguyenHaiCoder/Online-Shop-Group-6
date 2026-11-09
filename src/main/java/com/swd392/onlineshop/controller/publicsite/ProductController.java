package com.swd392.onlineshop.controller.publicsite;

import com.swd392.onlineshop.dto.request.ProductFilter;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(@ModelAttribute ProductFilter filter, Model model) {
        Page<Product> page = productService.getPublicProducts(filter);
        model.addAttribute("page", page);
        model.addAttribute("categoryCounts", productService.getProductCountByCategory());
        model.addAttribute("filter", filter);
        return "public/products/product-list";
    }

    @GetMapping("/{id}")
    public String productDetails(@PathVariable("id") Long id, Model model) {
        Product product = productService.getPublicProductById(id);
        model.addAttribute("product", product);
        return "public/products/product-details";
    }
}

