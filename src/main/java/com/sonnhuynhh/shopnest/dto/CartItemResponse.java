// src/main/java/com/sonnhuynhh/shopnest/dto/CartItemResponse.java
package com.sonnhuynhh.shopnest.dto;

public record CartItemResponse(
        Long itemId,
        Long productId,
        String productName,
        String imageUrl,
        java.math.BigDecimal price,
        int quantity,
        java.math.BigDecimal subtotal
) {}
