package com.sonnhuynhh.shopnest.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
    Long id,
    Long productId,
    String productName,
    Long userId,
    String userName,
    String userAvatar,
    Integer rating,
    String comment,
    LocalDateTime createdAt,
    boolean isOwner
) {}
