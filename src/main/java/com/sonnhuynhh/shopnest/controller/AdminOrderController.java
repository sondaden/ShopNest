package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.OrderResponse;
import com.sonnhuynhh.shopnest.model.OrderStatus;
import com.sonnhuynhh.shopnest.model.PaymentStatus;
import com.sonnhuynhh.shopnest.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            return ResponseEntity.ok(orderService.getAllOrders());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Lỗi tải danh sách đơn hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            OrderStatus status = OrderStatus.valueOf(statusStr);
            OrderResponse order = orderService.updateStatus(id, status);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công",
                    "order", order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Trạng thái không hợp lệ"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("paymentStatus");
            PaymentStatus paymentStatus = PaymentStatus.valueOf(statusStr);
            OrderResponse order = orderService.updatePaymentStatus(id, paymentStatus);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thanh toán thành công",
                    "order", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getOrderStatistics() {
        return ResponseEntity.ok(orderService.getOrderStatistics());
    }
}
