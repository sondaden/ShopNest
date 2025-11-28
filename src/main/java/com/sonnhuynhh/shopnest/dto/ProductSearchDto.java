// src/main/java/com/sonnhuynhh/shopnest/dto/ProductSearchDto.java
package com.sonnhuynhh.shopnest.dto;

import java.math.BigDecimal;

public record ProductSearchDto(
        Long id,
        String name,
        String slug,
        String imageUrl,
        BigDecimal price,
        Double rating,
        Integer stock,
        Long categoryId,
        String categoryName,
        Long brandId,
        String brandName
) {}