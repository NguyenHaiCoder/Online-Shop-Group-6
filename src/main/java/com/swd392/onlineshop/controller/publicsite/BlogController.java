package com.swd392.onlineshop.controller.publicsite;

import com.swd392.onlineshop.dto.request.PostFilter;
import com.swd392.onlineshop.entity.Post;
import com.swd392.onlineshop.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final PostService postService;

    @GetMapping
    public String listBlogs(@ModelAttribute PostFilter filter, Model model) {
        Page<Post> page = postService.getPublicPosts(filter);
        model.addAttribute("page", page);
        return "public/blogs/blog-list";
    }

    @GetMapping("/{id}")
    public String blogDetails(@PathVariable("id") Long id, Model model) {
        Post post = postService.getPublicPostById(id);
        model.addAttribute("post", post);
        return "public/blogs/blog-details";
    }
}

