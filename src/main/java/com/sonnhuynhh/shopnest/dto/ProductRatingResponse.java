package com.sonnhuynhh.shopnest.dto;

import java.util.Map;

public record ProductRatingResponse(
    Long productId,
    Double averageRating,
    Long totalReviews,
    Map<Integer, Long> ratingDistribution // star -> count
) {}
