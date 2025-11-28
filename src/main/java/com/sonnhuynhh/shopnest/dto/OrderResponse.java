// src/main/java/com/sonnhuynhh/shopnest/dto/OrderResponse.java
package com.sonnhuynhh.shopnest.dto;

import com.sonnhuynhh.shopnest.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderCode,
        BigDecimal totalAmount,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String shippingAddress,
        String customerName,
        String phone,
        String note,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
    public record OrderItemResponse(
            Long productId,
            String productName,
            String productImage,
            BigDecimal price,
            int quantity,
            BigDecimal subtotal
    ) {}
}