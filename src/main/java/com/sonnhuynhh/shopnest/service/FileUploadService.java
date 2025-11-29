package com.sonnhuynhh.shopnest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý upload file
 */
@Service
public class FileUploadService {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Upload avatar cho user
     * @param file MultipartFile cần upload
     * @param userId ID của user (dùng để tạo tên file unique)
     * @return Đường dẫn tương đối của file đã upload
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        validateImageFile(file);

        // Tạo thư mục avatars nếu chưa có
        Path avatarDir = Paths.get(uploadPath, "avatars");
        if (!Files.exists(avatarDir)) {
            Files.createDirectories(avatarDir);
        }

        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // Lưu file
        Path targetPath = avatarDir.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn tương đối để lưu vào database
        return "/uploads/avatars/" + newFilename;
    }

    /**
     * Upload ảnh sản phẩm
     * @param file MultipartFile cần upload
     * @return Đường dẫn tương đối của file đã upload
     */
    public String uploadProductImage(MultipartFile file) throws IOException {
        validateImageFile(file);

        // Tạo thư mục products nếu chưa có
        Path productDir = Paths.get(uploadPath, "products");
        if (!Files.exists(productDir)) {
            Files.createDirectories(productDir);
        }

        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = "product_" + UUID.randomUUID().toString() + extension;

        // Lưu file
        Path targetPath = productDir.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/products/" + newFilename;
    }

    /**
     * Xóa file đã upload
     * @param filePath Đường dẫn tương đối của file cần xóa
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        try {
            // Chuyển từ URL path sang file path
            String relativePath = filePath.replace("/uploads/", "");
            Path path = Paths.get(uploadPath, relativePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log error but don't throw - deletion failure shouldn't break the flow
            System.err.println("Failed to delete file: " + filePath + " - " + e.getMessage());
        }
    }

    /**
     * Validate file ảnh
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (JPEG, PNG, GIF, WebP)");
        }
    }

    /**
     * Lấy extension của file
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
