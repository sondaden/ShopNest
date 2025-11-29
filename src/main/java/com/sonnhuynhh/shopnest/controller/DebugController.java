package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.OrderResponse;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import com.sonnhuynhh.shopnest.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping("/my-orders")
    public ResponseEntity<?> myOrders(Authentication authentication) {
        Map<String, Object> res = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            res.put("error", "not_authenticated");
            return ResponseEntity.status(401).body(res);
        }

        String identifier = authentication.getName();
        User user = userRepository.findByUsername(identifier).or(() -> userRepository.findByEmail(identifier)).orElse(null);
        res.put("principalName", identifier);
        res.put("userFound", user != null);
        if (user != null) {
            res.put("userId", user.getId());
            res.put("username", user.getUsername());
            res.put("email", user.getEmail());
        }

        try {
            List<OrderResponse> orders = orderService.getMyOrders();
            res.put("ordersCount", orders.size());
            res.put("orders", orders);
        } catch (Exception e) {
            res.put("ordersError", e.getMessage());
        }

        return ResponseEntity.ok(res);
    }
}
