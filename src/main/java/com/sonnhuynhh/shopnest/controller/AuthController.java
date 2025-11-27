// src/main/java/com/sonnhuynhh/shopnest/controller/AuthController.java
package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.auth.*;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.model.Role;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import com.sonnhuynhh.shopnest.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserDetailsService userDetailsService;

    @PostMapping("/register")
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.username());
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByUsername(req.username()).get();
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @GetMapping("/me")
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
}