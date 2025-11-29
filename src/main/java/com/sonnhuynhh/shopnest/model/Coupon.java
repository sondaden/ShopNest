package com.sonnhuynhh.shopnest.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderAmount;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;
    
    private Integer usageLimit;
    
    @Builder.Default
    private Integer usedCount = 0;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Builder.Default
    private Boolean active = true;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if active
        if (!active) return false;
        
        // Check date range
        if (startDate != null && now.isBefore(startDate)) return false;
        if (endDate != null && now.isAfter(endDate)) return false;
        
        // Check usage limit
        if (usageLimit != null && usedCount >= usageLimit) return false;
        
        return true;
    }
    
    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (!isValid()) {
            return BigDecimal.ZERO;
        }
        
        // Check minimum order amount
        if (minOrderAmount != null && orderTotal.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount;
        
        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderTotal.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            discount = discountValue;
        }
        
        // Apply max discount limit
        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }
        
        // Discount cannot exceed order total
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        
        return discount;
    }
}
