package com.swd392.onlineshop.controller.publicsite;

import com.swd392.onlineshop.config.AppProperties;
import com.swd392.onlineshop.service.PostService;
import com.swd392.onlineshop.service.ProductService;
import com.swd392.onlineshop.service.SliderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SliderService sliderService;
    private final ProductService productService;
    private final PostService postService;
    private final AppProperties appProperties;

    @GetMapping("/")
    public String home(Model model) {
        int featuredSize = appProperties.getHome().getFeaturedSize() > 0 ? appProperties.getHome().getFeaturedSize() : 8;

        model.addAttribute("sliders", sliderService.getVisibleSliders());
        model.addAttribute("featuredProducts", productService.getFeaturedProducts(featuredSize));
        model.addAttribute("hotPosts", postService.getFeaturedPosts(4));
        return "public/home/home";
    }
}
