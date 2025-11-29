// src/main/java/com/sonnhuynhh/shopnest/controller/AuthController.java
package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.auth.*;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.model.Role;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import com.sonnhuynhh.shopnest.service.JwtService;
import com.sonnhuynhh.shopnest.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private PasswordResetService passwordResetService;

    // ============ API Endpoints ============
    
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        user.setRole(req.username().equalsIgnoreCase("admin") ? Role.ADMIN : Role.USER);

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.username());
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByUsername(req.username()).get();
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public String whoAmI(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Chưa đăng nhập";
        }
        return """
            Đã đăng nhập thành công!
            Username: %s
            Authorities: %s
            """.formatted(
                authentication.getName(),
                authentication.getAuthorities()
        );
    }
    
    // ============ Forgot Password - View Endpoints ============
    
    /**
     * Trang quên mật khẩu - Bước 1: Nhập email
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }
    
    /**
     * Xử lý gửi OTP
     */
    @PostMapping("/forgot-password/send-otp")
    public String sendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        boolean sent = passwordResetService.sendOtp(email);
        
        if (sent) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("otpSent", true);
            return "redirect:/forgot-password/verify";
        } else {
            redirectAttributes.addFlashAttribute("error", "Email không tồn tại trong hệ thống!");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot-password";
        }
    }
    
    /**
     * Trang xác nhận OTP - Bước 2
     */
    @GetMapping("/forgot-password/verify")
    public String verifyOtpPage(@ModelAttribute("email") String email, 
                                 @ModelAttribute("otpSent") String otpSent,
                                 Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/forgot-password";
        }
        model.addAttribute("email", email);
        return "forgot-password-verify";
    }
    
    /**
     * Xử lý xác nhận OTP và đặt mật khẩu mới
     */
    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String otp,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        // Validate mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot-password/verify";
        }
        
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot-password/verify";
        }
        
        // Xác nhận OTP và đặt lại mật khẩu
        boolean success = passwordResetService.resetPassword(email, otp, newPassword);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Mã OTP không đúng hoặc đã hết hạn!");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot-password/verify";
        }
    }
    
    /**
     * Gửi lại OTP
     */
    @PostMapping("/forgot-password/resend-otp")
    public String resendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        boolean sent = passwordResetService.sendOtp(email);
        
        if (sent) {
            redirectAttributes.addFlashAttribute("success", "Đã gửi lại mã OTP!");
            redirectAttributes.addFlashAttribute("email", email);
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể gửi OTP. Vui lòng thử lại!");
            redirectAttributes.addFlashAttribute("email", email);
        }
        return "redirect:/forgot-password/verify";
    }
}