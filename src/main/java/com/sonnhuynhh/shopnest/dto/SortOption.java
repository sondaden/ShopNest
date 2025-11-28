// src/main/java/com/sonnhuynhh/shopnest/dto/SortOption.java
package com.sonnhuynhh.shopnest.dto;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum SortOption {
    NEWEST("createdAt", Sort.Direction.DESC),
    PRICE_ASC("price", Sort.Direction.ASC),
    PRICE_DESC("price", Sort.Direction.DESC),
    RATING_DESC("rating", Sort.Direction.DESC);

    private final String field;
    private final Sort.Direction direction;

    SortOption(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }

    public Sort getSort() {
        return Sort.by(direction, field);
    }
}