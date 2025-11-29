package com.sonnhuynhh.shopnest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cấu hình để serve file upload từ thư mục bên ngoài
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose thư mục uploads để có thể truy cập qua URL
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        String uploadAbsolutePath = uploadDir.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadAbsolutePath);
    }
}
