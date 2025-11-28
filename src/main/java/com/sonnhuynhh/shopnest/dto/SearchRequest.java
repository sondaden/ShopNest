// src/main/java/com/sonnhuynhh/shopnest/dto/SearchRequest.java
package com.sonnhuynhh.shopnest.dto;

import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

public record SearchRequest(
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long brandId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(defaultValue = "NEWEST") SortOption sort
) {
    // Constructor bắt buộc để Spring bind được
    public SearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0 || size > 100) size = 20;
        if (sort == null) sort = SortOption.NEWEST;
    }
}