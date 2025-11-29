package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.WishlistItemResponse;
import com.sonnhuynhh.shopnest.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    
    private final WishlistService wishlistService;
    
    /**
     * Get user's wishlist
     */
    @GetMapping
    public ResponseEntity<List<WishlistItemResponse>> getWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wishlistService.getUserWishlist(userDetails.getUsername()));
    }
    
    /**
     * Get wishlist count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getWishlistCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = wishlistService.getWishlistCount(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * Get all product IDs in wishlist
     */
    @GetMapping("/products")
    public ResponseEntity<List<Long>> getWishlistProductIds(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wishlistService.getWishlistProductIds(userDetails.getUsername()));
    }
    
    /**
     * Check if product is in wishlist
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean inWishlist = wishlistService.isInWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }
    
    /**
     * Add product to wishlist
     */
    @PostMapping("/{productId}")
    public ResponseEntity<?> addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            WishlistItemResponse item = wishlistService.addToWishlist(userDetails.getUsername(), productId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã thêm vào danh sách yêu thích",
                "item", item
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Remove product from wishlist
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            wishlistService.removeFromWishlist(userDetails.getUsername(), productId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa khỏi danh sách yêu thích"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Toggle product in wishlist
     */
    @PostMapping("/{productId}/toggle")
    public ResponseEntity<?> toggleWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            boolean added = wishlistService.toggleWishlist(userDetails.getUsername(), productId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "added", added,
                "message", added ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Clear entire wishlist
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            wishlistService.clearWishlist(userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa toàn bộ danh sách yêu thích"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
