package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.model.Role;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import com.sonnhuynhh.shopnest.service.IProductService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for rendering Thymeleaf pages
 * Calls API services to get data and renders to templates
 */
@Controller
public class PageController {

    private final IProductService productService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PageController(IProductService productService, 
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.productService = productService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Home page - displays featured products
     */
    @GetMapping("/")
    public String index(Model model) {
        try {
            List<Product> products = productService.getAllProducts();
            model.addAttribute("products", products);
            model.addAttribute("pageTitle", "Trang chủ");
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách sản phẩm. Vui lòng thử lại sau.");
        }
        return "index";
    }

    /**
     * Products listing page
     */
    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        try {
            List<Product> products = productService.getAllProducts();
            // TODO: Add filtering by category and search
            model.addAttribute("products", products);
            model.addAttribute("pageTitle", "Sản phẩm");
            model.addAttribute("category", category);
            model.addAttribute("search", search);
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách sản phẩm. Vui lòng thử lại sau.");
        }
        return "index"; // Reuse index template for now
    }

    /**
     * Login page
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
        }
        if (logout != null) {
            model.addAttribute("message", "Đăng xuất thành công!");
        }
        model.addAttribute("pageTitle", "Đăng nhập");
        return "login";
    }

    /**
     * Registration page
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Đăng ký");
        return "register";
    }

    /**
     * Handle registration form submission
     */
    @PostMapping("/register")
    public String processRegister(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            model.addAttribute("username", username);
            model.addAttribute("pageTitle", "Đăng ký");
            return "register";
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            model.addAttribute("pageTitle", "Đăng ký");
            return "register";
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email đã được sử dụng!");
            model.addAttribute("fullName", fullName);
            model.addAttribute("phone", phone);
            model.addAttribute("username", username);
            model.addAttribute("pageTitle", "Đăng ký");
            return "register";
        }

        try {
            // Create new user
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(Role.USER);

            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Đã có lỗi xảy ra. Vui lòng thử lại!");
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            model.addAttribute("username", username);
            model.addAttribute("pageTitle", "Đăng ký");
            return "register";
        }
    }

    /**
     * Cart page
     */
    @GetMapping("/cart")
    public String cart(Model model) {
        // TODO: Get cart items from session or user
        // For now, display empty cart
        model.addAttribute("pageTitle", "Giỏ hàng");
        model.addAttribute("subtotal", 0);
        model.addAttribute("shippingFee", 0);
        model.addAttribute("discount", 0);
        model.addAttribute("total", 0);
        return "cart";
    }
}
