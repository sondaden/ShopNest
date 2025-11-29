package com.sonnhuynhh.shopnest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private Path uploadDir;

    @PostConstruct
    public void init() {
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload!", e);
        }
    }

    /**
     * Upload một file ảnh
     */
    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("error", "Chỉ chấp nhận file ảnh (jpg, png, gif, webp)");
                return ResponseEntity.badRequest().body(response);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;

            // Create products subdirectory
            Path productImagesDir = uploadDir.resolve("products");
            Files.createDirectories(productImagesDir);

            // Save file
            Path targetLocation = productImagesDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return URL
            String imageUrl = "/uploads/products/" + newFilename;
            
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("filename", newFilename);
            response.put("originalName", originalFilename);
            response.put("size", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Lỗi khi upload file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Upload nhiều file ảnh cùng lúc
     */
    @PostMapping("/images")
    public ResponseEntity<Map<String, Object>> uploadMultipleImages(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    errors.add("File trống: " + file.getOriginalFilename());
                    continue;
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    errors.add("Không phải file ảnh: " + file.getOriginalFilename());
                    continue;
                }

                String originalFilename = file.getOriginalFilename();
                String extension = getFileExtension(originalFilename);
                String newFilename = UUID.randomUUID().toString() + extension;

                Path productImagesDir = uploadDir.resolve("products");
                Files.createDirectories(productImagesDir);

                Path targetLocation = productImagesDir.resolve(newFilename);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                String imageUrl = "/uploads/products/" + newFilename;

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("url", imageUrl);
                fileInfo.put("filename", newFilename);
                fileInfo.put("originalName", originalFilename);
                fileInfo.put("size", file.getSize());
                uploadedFiles.add(fileInfo);

            } catch (IOException e) {
                errors.add("Lỗi upload " + file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        response.put("success", !uploadedFiles.isEmpty());
        response.put("files", uploadedFiles);
        response.put("uploadedCount", uploadedFiles.size());
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Xóa file ảnh
     */
    @DeleteMapping("/image")
    public ResponseEntity<Map<String, Object>> deleteImage(@RequestParam("url") String imageUrl) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract filename from URL
            String filename = imageUrl.replace("/uploads/products/", "");
            Path filePath = uploadDir.resolve("products").resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                response.put("success", true);
                response.put("message", "Đã xóa file thành công");
            } else {
                response.put("success", false);
                response.put("error", "File không tồn tại");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Lỗi khi xóa file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
