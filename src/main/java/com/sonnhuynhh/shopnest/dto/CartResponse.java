// src/main/java/com/sonnhuynhh/shopnest/dto/CartResponse.java
package com.sonnhuynhh.shopnest.dto;

import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        int totalItems,
        java.math.BigDecimal totalPrice
) {}
