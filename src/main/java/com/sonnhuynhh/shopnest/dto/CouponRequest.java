package com.sonnhuynhh.shopnest.dto;

import com.sonnhuynhh.shopnest.model.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    
    @NotBlank(message = "Mã giảm giá không được để trống")
    private String code;
    
    private String description;
    
    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;
    
    @NotNull(message = "Giá trị giảm không được để trống")
    @Positive(message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;
    
    private BigDecimal minOrderAmount;
    
    private BigDecimal maxDiscountAmount;
    
    private Integer usageLimit;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Boolean active = true;
}
