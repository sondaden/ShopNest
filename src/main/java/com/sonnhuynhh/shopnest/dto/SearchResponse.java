// src/main/java/com/sonnhuynhh/shopnest/dto/SearchResponse.java
package com.sonnhuynhh.shopnest.dto;

import java.util.List;

public record SearchResponse(
        List<ProductSearchDto> items,
        long total,
        int page,
        int size,
        List<String> suggestions
) {}