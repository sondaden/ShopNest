package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.CouponRequest;
import com.sonnhuynhh.shopnest.dto.CouponResponse;
import com.sonnhuynhh.shopnest.dto.CouponValidationResult;
import com.sonnhuynhh.shopnest.model.Coupon;
import com.sonnhuynhh.shopnest.model.DiscountType;
import com.sonnhuynhh.shopnest.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    
    private final CouponRepository couponRepository;
    
    // =================== ADMIN METHODS ===================
    
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        return CouponResponse.fromEntity(coupon);
    }
    
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new RuntimeException("Mã giảm giá đã tồn tại");
        }
        
        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        
        Coupon saved = couponRepository.save(coupon);
        return CouponResponse.fromEntity(saved);
    }
    
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        
        // Check if code changed and new code exists
        if (!coupon.getCode().equalsIgnoreCase(request.getCode())) {
            if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
                throw new RuntimeException("Mã giảm giá đã tồn tại");
            }
            coupon.setCode(request.getCode().toUpperCase());
        }
        
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        
        if (request.getActive() != null) {
            coupon.setActive(request.getActive());
        }
        
        Coupon saved = couponRepository.save(coupon);
        return CouponResponse.fromEntity(saved);
    }
    
    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy mã giảm giá");
        }
        couponRepository.deleteById(id);
    }
    
    @Transactional
    public CouponResponse toggleCouponStatus(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        coupon.setActive(!coupon.getActive());
        Coupon saved = couponRepository.save(coupon);
        return CouponResponse.fromEntity(saved);
    }
    
    // =================== USER METHODS ===================
    
    public List<CouponResponse> getValidCoupons() {
        return couponRepository.findAllValidCoupons(LocalDateTime.now()).stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public CouponValidationResult validateCoupon(String code, BigDecimal orderTotal) {
        if (code == null || code.isBlank()) {
            return CouponValidationResult.invalid("Vui lòng nhập mã giảm giá");
        }
        
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElse(null);
        
        if (coupon == null) {
            return CouponValidationResult.invalid("Mã giảm giá không tồn tại");
        }
        
        if (!coupon.getActive()) {
            return CouponValidationResult.invalid("Mã giảm giá đã bị vô hiệu hóa");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            return CouponValidationResult.invalid("Mã giảm giá chưa có hiệu lực");
        }
        
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            return CouponValidationResult.invalid("Mã giảm giá đã hết hạn");
        }
        
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return CouponValidationResult.invalid("Mã giảm giá đã hết lượt sử dụng");
        }
        
        if (coupon.getMinOrderAmount() != null && orderTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            return CouponValidationResult.invalid(
                String.format("Đơn hàng tối thiểu %,.0f₫ để sử dụng mã này", 
                    coupon.getMinOrderAmount().doubleValue())
            );
        }
        
        BigDecimal discountAmount = coupon.calculateDiscount(orderTotal);
        
        String message;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            message = String.format("Giảm %,.0f%% cho đơn hàng", coupon.getDiscountValue().doubleValue());
        } else {
            message = String.format("Giảm %,.0f₫ cho đơn hàng", coupon.getDiscountValue().doubleValue());
        }
        
        return CouponValidationResult.valid(coupon.getCode(), orderTotal, discountAmount, message);
    }
    
    @Transactional
    public void useCoupon(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));
        
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }
    
    public BigDecimal applyCoupon(String code, BigDecimal orderTotal) {
        CouponValidationResult result = validateCoupon(code, orderTotal);
        if (!result.isValid()) {
            throw new RuntimeException(result.getMessage());
        }
        return result.getDiscountAmount();
    }
}
