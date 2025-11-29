package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.model.Role;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import com.sonnhuynhh.shopnest.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller xử lý các trang frontend (Thymeleaf views)
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Trang chủ - hiển thị sản phẩm nổi bật
     */
    @GetMapping("/")
    public String home(Model model) {
        try {
            List<Product> products = productService.getAllProducts();
            // Giới hạn 8 sản phẩm cho trang chủ
            if (products.size() > 8) {
                products = products.subList(0, 8);
            }
            model.addAttribute("products", products);
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách sản phẩm: " + e.getMessage());
        }
        return "index";
    }

    /**
     * Trang đăng nhập
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Trang đăng ký - GET
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * Xử lý đăng ký - POST
     */
    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            Model model) {
        
        try {
            // Kiểm tra username đã tồn tại
            if (userRepository.existsByUsername(username)) {
                model.addAttribute("error", "Tên đăng nhập đã tồn tại");
                model.addAttribute("username", username);
                model.addAttribute("email", email);
                model.addAttribute("fullName", fullName);
                model.addAttribute("phone", phone);
                return "register";
            }

            // Kiểm tra email đã tồn tại
            if (userRepository.existsByEmail(email)) {
                model.addAttribute("error", "Email đã được sử dụng");
                model.addAttribute("username", username);
                model.addAttribute("email", email);
                model.addAttribute("fullName", fullName);
                model.addAttribute("phone", phone);
                return "register";
            }

            // Tạo user mới
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole(username.equalsIgnoreCase("admin") ? Role.ADMIN : Role.USER);

            userRepository.save(user);

            // Redirect đến trang login với thông báo thành công
            return "redirect:/login?registered=true";
            
        } catch (Exception e) {
            model.addAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "register";
        }
    }
}
