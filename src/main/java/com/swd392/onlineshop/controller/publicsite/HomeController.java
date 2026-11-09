package com.swd392.onlineshop.controller.publicsite;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", List.of());
        model.addAttribute("hotPosts", List.of());
        return "public/home/home";
    }
}
