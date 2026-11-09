package com.swd392.onlineshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;

/** Legacy demo image paths use a visible empty state until real images are uploaded. */
@Configuration
public class ImageResourceConfig implements WebMvcConfigurer {
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/")
            .resourceChain(true).addResolver(new PathResourceResolver() {
                @Override protected Resource getResource(String path, Resource location) throws IOException {
                    Resource existing = super.getResource(path, location);
                    if (existing != null) return existing;
                    String fallback = path.startsWith("sliders/") ? "nova-workspace.png" : "image-unavailable.svg";
                    Resource resource = location.createRelative(fallback);
                    return resource.isReadable() ? resource : null;
                }
            });
    }
}