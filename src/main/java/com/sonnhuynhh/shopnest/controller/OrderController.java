// src/main/java/com/sonnhuynhh/shopnest/controller/OrderController.java
package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.*;
import com.sonnhuynhh.shopnest.model.OrderStatus;
import com.sonnhuynhh.shopnest.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request.status()));
    }
}

// DTO nhỏ cho admin update
record UpdateStatusRequest(OrderStatus status) {}