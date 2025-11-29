package com.sonnhuynhh.shopnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResult {
    private boolean valid;
    private String message;
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal originalTotal;
    private BigDecimal finalTotal;
    
    public static CouponValidationResult invalid(String message) {
        return CouponValidationResult.builder()
                .valid(false)
                .message(message)
                .build();
    }
    
    public static CouponValidationResult valid(String code, BigDecimal originalTotal, 
                                                BigDecimal discountAmount, String message) {
        return CouponValidationResult.builder()
                .valid(true)
                .code(code)
                .message(message)
                .originalTotal(originalTotal)
                .discountAmount(discountAmount)
                .finalTotal(originalTotal.subtract(discountAmount))
                .build();
    }
}
