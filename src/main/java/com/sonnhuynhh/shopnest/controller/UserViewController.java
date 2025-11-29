package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.OrderResponse;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import com.sonnhuynhh.shopnest.service.FileUploadService;
import com.sonnhuynhh.shopnest.service.OrderService;
import com.sonnhuynhh.shopnest.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

/**
 * Controller xử lý các trang user (profile, orders, wishlist)
 */
@Controller
@RequiredArgsConstructor
public class UserViewController {

    private final UserRepository userRepository;
    private final OrderService orderService;
    private final ProductService productService;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    /**
     * Helper method để tìm user (hỗ trợ cả OAuth2 users)
     */
    private User findCurrentUser(Authentication authentication) {
        String identifier = authentication.getName();
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElse(null);
    }
    
    private User findCurrentUserOrThrow(Authentication authentication) {
        String identifier = authentication.getName();
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("User not found: " + identifier));
    }

    /**
     * Trang thông tin cá nhân
     */
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        try {
            User user = findCurrentUser(authentication);
            model.addAttribute("user", user);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "profile";
    }

    /**
     * Trang lịch sử đơn hàng
     */
    @GetMapping("/orders")
    public String orders(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        try {
            List<OrderResponse> orders = orderService.getMyOrders();
            
            // Transform orders to add display-friendly fields
            List<OrderDisplayDto> displayOrders = orders.stream()
                    .map(order -> {
                        String firstItemName = (order.items() != null && !order.items().isEmpty()) 
                                ? order.items().get(0).productName() 
                                : "Không có sản phẩm";
                        int itemsCount = (order.items() != null) ? order.items().size() : 0;
                        
                        return new OrderDisplayDto(
                                order.id(),
                                order.orderCode(),
                                order.createdAt(),
                                firstItemName,
                                itemsCount,
                                order.totalAmount(),
                                order.status() != null ? order.status().name() : "PENDING",
                                getStatusLabel(order.status() != null ? order.status().name() : "PENDING")
                        );
                    })
                    .toList();
            
            model.addAttribute("orders", displayOrders);
            model.addAttribute("totalOrders", displayOrders.size());
            
            // Recommended products
            model.addAttribute("recommendedProducts", productService.getAllProducts().stream().limit(3).toList());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("orders", Collections.emptyList());
            model.addAttribute("totalOrders", 0);
            model.addAttribute("recommendedProducts", Collections.emptyList());
            model.addAttribute("error", e.getMessage());
        }
        return "order-history";
    }

    /**
     * Trang chi tiết đơn hàng
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        try {
            List<OrderResponse> orders = orderService.getMyOrders();
            OrderResponse order = orders.stream()
                    .filter(o -> o.id().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            model.addAttribute("order", order);
            model.addAttribute("statusLabel", getStatusLabel(order.status().name()));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/orders";
        }
        return "order-detail";
    }

    /**
     * Trang sản phẩm yêu thích (placeholder - chưa implement WishlistService)
     */
    @GetMapping("/wishlist")
    public String wishlist(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        // TODO: Implement WishlistService
        model.addAttribute("wishlist", Collections.emptyList());
        return "wishlist";
    }

    /**
     * Trang đổi mật khẩu
     */
    @GetMapping("/change-password")
    public String changePassword(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        
        try {
            User user = findCurrentUser(authentication);
            model.addAttribute("user", user);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "change-password";
    }
    
    /**
     * Cập nhật thông tin cá nhân
     */
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String avatarUrl,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        
        try {
            User user = findCurrentUserOrThrow(authentication);
            
            user.setFullName(fullName);
            user.setPhone(phone);
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                user.setAvatarUrl(avatarUrl);
            }
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/profile";
    }
    
    /**
     * Upload avatar
     */
    @PostMapping("/profile/upload-avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        
        try {
            User user = findCurrentUserOrThrow(authentication);
            
            // Xóa avatar cũ nếu có (chỉ xóa file upload, không xóa URL external)
            String oldAvatar = user.getAvatarUrl();
            if (oldAvatar != null && oldAvatar.startsWith("/uploads/")) {
                fileUploadService.deleteFile(oldAvatar);
            }
            
            // Upload avatar mới
            String avatarPath = fileUploadService.uploadAvatar(file, user.getId());
            user.setAvatarUrl(avatarPath);
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ảnh đại diện thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi upload: " + e.getMessage());
        }
        return "redirect:/profile";
    }
    
    /**
     * Xử lý đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam String currentPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam String confirmPassword,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        
        try {
            User user = findCurrentUserOrThrow(authentication);
            
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/change-password";
            }
            
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không khớp!");
                return "redirect:/change-password";
            }
            
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return "redirect:/change-password";
            }
            
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/change-password";
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "Đang xử lý";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPING" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    /**
     * DTO để hiển thị đơn hàng trong danh sách
     */
    public record OrderDisplayDto(
            Long id,
            String code,
            java.time.LocalDateTime createdAt,
            String firstItemName,
            int itemsCount,
            java.math.BigDecimal totalAmount,
            String status,
            String statusLabel
    ) {}
}
