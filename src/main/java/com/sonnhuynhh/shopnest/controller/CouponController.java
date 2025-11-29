package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.CouponRequest;
import com.sonnhuynhh.shopnest.dto.CouponResponse;
import com.sonnhuynhh.shopnest.dto.CouponValidationResult;
import com.sonnhuynhh.shopnest.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponService couponService;
    
    // =================== USER ENDPOINTS ===================
    
    /**
     * Get all valid coupons for users
     */
    @GetMapping("/valid")
    public ResponseEntity<List<CouponResponse>> getValidCoupons() {
        return ResponseEntity.ok(couponService.getValidCoupons());
    }
    
    /**
     * Validate a coupon code
     */
    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResult> validateCoupon(
            @RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        BigDecimal orderTotal = new BigDecimal(request.get("orderTotal").toString());
        
        CouponValidationResult result = couponService.validateCoupon(code, orderTotal);
        return ResponseEntity.ok(result);
    }
    
    // =================== ADMIN ENDPOINTS ===================
    
    /**
     * Get all coupons (Admin)
     */
    @GetMapping("/admin")
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }
    
    /**
     * Get coupon by ID (Admin)
     */
    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getCouponById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(couponService.getCouponById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Create new coupon (Admin)
     */
    @PostMapping("/admin")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CouponRequest request) {
        try {
            CouponResponse coupon = couponService.createCoupon(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo mã giảm giá thành công",
                "coupon", coupon
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Update coupon (Admin)
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateCoupon(
            @PathVariable Long id, 
            @Valid @RequestBody CouponRequest request) {
        try {
            CouponResponse coupon = couponService.updateCoupon(id, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật mã giảm giá thành công",
                "coupon", coupon
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Delete coupon (Admin)
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa mã giảm giá thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Toggle coupon status (Admin)
     */
    @PutMapping("/admin/{id}/toggle")
    public ResponseEntity<?> toggleCouponStatus(@PathVariable Long id) {
        try {
            CouponResponse coupon = couponService.toggleCouponStatus(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", coupon.getActive() ? "Đã kích hoạt mã giảm giá" : "Đã vô hiệu hóa mã giảm giá",
                "coupon", coupon
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
