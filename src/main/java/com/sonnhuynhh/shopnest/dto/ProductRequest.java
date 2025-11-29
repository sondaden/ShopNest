// src/main/java/com/sonnhuynhh/shopnest/dto/ProductRequest.java
package com.sonnhuynhh.shopnest.dto;

import java.util.List;

public record ProductRequest(
        String name,
        java.math.BigDecimal price,
        Integer stock,
        String description,
        String imageUrl,
        List<String> imageUrls,  // Danh sách URL ảnh
        Long categoryId,
        Long brandId,
        Double rating
) {}