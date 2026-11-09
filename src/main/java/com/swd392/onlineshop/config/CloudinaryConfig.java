package com.swd392.onlineshop.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", properties.getCloudName() == null ? "" : properties.getCloudName());
        config.put("api_key", properties.getApiKey() == null ? "" : properties.getApiKey());
        config.put("api_secret", properties.getApiSecret() == null ? "" : properties.getApiSecret());
        config.put("secure", "true");
        return new Cloudinary(config);
    }
}
