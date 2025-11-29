package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.model.*;
import com.sonnhuynhh.shopnest.repository.*;
import com.sonnhuynhh.shopnest.service.*;
import com.sonnhuynhh.shopnest.dto.ProductRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== DASHBOARD ====================
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        // Stats
        long totalProducts = productRepository.count();
        long totalCustomers = userRepository.countByRole(Role.USER);
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        
        // Today's revenue (paid orders today)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        BigDecimal todayRevenue = orderRepository.sumTotalAmountByPaymentStatusAndCreatedAtAfter(
            PaymentStatus.PAID, startOfDay);
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }
        
        // Monthly revenue
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        BigDecimal monthlyRevenue = orderRepository.sumTotalAmountByPaymentStatusAndCreatedAtBetween(
            PaymentStatus.PAID, startOfMonth, endOfMonth);
        if (monthlyRevenue == null) {
            monthlyRevenue = BigDecimal.ZERO;
        }
        
        // Today's orders count
        long todayOrdersCount = orderRepository.countByCreatedAtAfter(startOfDay);
        
        // Recent orders
        List<Order> recentOrders = orderRepository.findTop10ByOrderByCreatedAtDesc();
        
        // Top products (most recent products as placeholder)
        List<Product> topProducts = productRepository.findAllByOrderByIdDesc(PageRequest.of(0, 5));
        
        // Low stock products
        List<Product> lowStockProducts = productRepository.findByStockLessThanOrderByStockAsc(10);
        
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("newOrders", pendingOrders);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("todayOrdersCount", todayOrdersCount);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("activePage", "dashboard");
        
        return "admin/dashboard";
    }

    // ==================== PRODUCTS ====================
    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            Model model) {
        
        Page<Product> productPage;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        
        if (search != null && !search.isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(search, pageRequest);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageRequest);
        } else {
            productPage = productRepository.findAll(pageRequest);
        }
        
        List<Category> categories = categoryRepository.findAll();
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("activePage", "products");
        
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", false);
        model.addAttribute("activePage", "products");
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", true);
        model.addAttribute("activePage", "products");
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute ProductRequest productRequest, 
                              @RequestParam(required = false) Long productId,
                              RedirectAttributes redirectAttributes) {
        try {
            if (productId != null) {
                productService.updateProduct(productId, productRequest);
                redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
            } else {
                productService.addProduct(productRequest);
                redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ==================== CATEGORIES ====================
    @GetMapping("/categories")
    public String categories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("activePage", "categories");
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) Long categoryId,
                               RedirectAttributes redirectAttributes) {
        try {
            Category category;
            if (categoryId != null) {
                category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            } else {
                category = new Category();
            }
            category.setName(name);
            category.setDescription(description);
            // Auto generate slug from name
            String slug = name.toLowerCase()
                .replaceAll("[đĐ]", "d")
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("^-|-$", "");
            category.setSlug(slug);
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("success", "Lưu danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục đang có sản phẩm!");
        }
        return "redirect:/admin/categories";
    }

    // ==================== ORDERS ====================
    @GetMapping("/orders")
    public String orders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model) {
        
        Page<Order> orderPage;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (status != null && !status.isEmpty()) {
            orderPage = orderRepository.findByStatus(OrderStatus.valueOf(status), pageRequest);
        } else {
            orderPage = orderRepository.findAll(pageRequest);
        }
        
        // Order status counts
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmedCount = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long shippingCount = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long deliveredCount = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledCount = orderRepository.countByStatus(OrderStatus.CANCELLED);
        
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("status", status);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("activePage", "orders");
        
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("activePage", "orders");
        return "admin/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus(OrderStatus.valueOf(status));
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // ==================== USERS ====================
    @GetMapping("/users")
    public String users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        Page<User> userPage;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        
        if (search != null && !search.isEmpty()) {
            userPage = userRepository.findByEmailContainingIgnoreCase(search, pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }
        
        // User stats
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long inactiveUsers = userRepository.countByActiveFalse();
        long adminCount = userRepository.countByRole(Role.ADMIN);
        long customerCount = userRepository.countByRole(Role.USER);
        
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("activePage", "users");
        
        return "admin/users";
    }

    // ==================== REPORTS ====================
    @GetMapping("/reports")
    public String reports(Model model) {
        // Total revenue (all paid orders)
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByPaymentStatusAndCreatedAtBetween(
            PaymentStatus.PAID, 
            LocalDateTime.of(2000, 1, 1, 0, 0), 
            LocalDateTime.now());
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        // Total orders
        long totalOrders = orderRepository.count();
        
        // Average order value
        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0 && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            avgOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, java.math.RoundingMode.HALF_UP);
        }
        
        // Order status counts
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmedCount = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long shippingCount = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long deliveredCount = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledCount = orderRepository.countByStatus(OrderStatus.CANCELLED);
        
        // Calculate percentages
        int pendingPercent = totalOrders > 0 ? (int) (pendingCount * 100 / totalOrders) : 0;
        int shippingPercent = totalOrders > 0 ? (int) (shippingCount * 100 / totalOrders) : 0;
        int deliveredPercent = totalOrders > 0 ? (int) (deliveredCount * 100 / totalOrders) : 0;
        int cancelledPercent = totalOrders > 0 ? (int) (cancelledCount * 100 / totalOrders) : 0;
        
        // Top products (recent products)
        List<Product> topProducts = productRepository.findAllByOrderByIdDesc(PageRequest.of(0, 5));
        
        // Recent orders
        List<Order> recentOrders = orderRepository.findTop10ByOrderByCreatedAtDesc();
        
        // Low stock products
        List<Product> lowStockProducts = productRepository.findByStockLessThanOrderByStockAsc(10);
        
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("avgOrderValue", avgOrderValue);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("pendingPercent", pendingPercent);
        model.addAttribute("shippingPercent", shippingPercent);
        model.addAttribute("deliveredPercent", deliveredPercent);
        model.addAttribute("cancelledPercent", cancelledPercent);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("activePage", "reports");
        return "admin/reports";
    }

    // ==================== SETTINGS ====================
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activePage", "settings");
        return "admin/settings";
    }
    
    // ==================== PROFILE ====================
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String identifier = authentication.getName();
        User admin = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElse(null);
        model.addAttribute("user", admin);
        model.addAttribute("activePage", "profile");
        return "admin/profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String avatarUrl,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            String identifier = authentication.getName();
            User admin = userRepository.findByUsername(identifier)
                    .or(() -> userRepository.findByEmail(identifier))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            admin.setFullName(fullName);
            admin.setPhone(phone);
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                admin.setAvatarUrl(avatarUrl);
            }
            userRepository.save(admin);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }
    
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            String identifier = authentication.getName();
            User admin = userRepository.findByUsername(identifier)
                    .or(() -> userRepository.findByEmail(identifier))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/admin/profile";
            }
            
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không khớp!");
                return "redirect:/admin/profile";
            }
            
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return "redirect:/admin/profile";
            }
            
            admin.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(admin);
            
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }
}
