// src/main/java/com/sonnhuynhh/shopnest/dto/ProductRequest.java
package com.sonnhuynhh.shopnest.dto;

public record ProductRequest(
        String name,
        java.math.BigDecimal price,
        Integer stock,
        String description,
        String imageUrl,
        Long categoryId,     // mới
        Long brandId,        // mới (có thể null)
        Double rating
) {}