package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.ProductRatingResponse;
import com.sonnhuynhh.shopnest.dto.ReviewRequest;
import com.sonnhuynhh.shopnest.dto.ReviewResponse;
import com.sonnhuynhh.shopnest.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<?> addReview(@Valid @RequestBody ReviewRequest request) {
        try {
            ReviewResponse review = reviewService.addReview(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đánh giá thành công!",
                "review", review
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        try {
            ReviewResponse review = reviewService.updateReview(id, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật đánh giá thành công!",
                "review", review
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa đánh giá thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }
    
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ProductRatingResponse> getProductRating(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductRating(productId));
    }
    
    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewResponse>> getMyReviews() {
        return ResponseEntity.ok(reviewService.getUserReviews());
    }
    
    @GetMapping("/product/{productId}/has-reviewed")
    public ResponseEntity<Map<String, Boolean>> hasUserReviewed(@PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("hasReviewed", reviewService.hasUserReviewed(productId)));
    }
}
