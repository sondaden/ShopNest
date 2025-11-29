package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.WishlistItemResponse;
import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.model.Wishlist;
import com.sonnhuynhh.shopnest.repository.ProductRepository;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import com.sonnhuynhh.shopnest.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {
    
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    /**
     * Get user's wishlist
     */
    public List<WishlistItemResponse> getUserWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(w -> WishlistItemResponse.fromProduct(w.getProduct(), w.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get wishlist count for user
     */
    public long getWishlistCount(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return 0;
        return wishlistRepository.countByUserId(user.getId());
    }
    
    /**
     * Check if product is in wishlist
     */
    public boolean isInWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;
        return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
    }
    
    /**
     * Get all product IDs in user's wishlist
     */
    public List<Long> getWishlistProductIds(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return List.of();
        return wishlistRepository.findProductIdsByUserId(user.getId());
    }
    
    /**
     * Add product to wishlist
     */
    @Transactional
    public WishlistItemResponse addToWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        // Check if already in wishlist
        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích");
        }
        
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();
        
        Wishlist saved = wishlistRepository.save(wishlist);
        return WishlistItemResponse.fromProduct(product, saved.getCreatedAt());
    }
    
    /**
     * Remove product from wishlist
     */
    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new RuntimeException("Sản phẩm không có trong danh sách yêu thích");
        }
        
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }
    
    /**
     * Toggle product in wishlist (add if not exists, remove if exists)
     */
    @Transactional
    public boolean toggleWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
            return false; // Removed
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .product(product)
                    .build();
            
            wishlistRepository.save(wishlist);
            return true; // Added
        }
    }
    
    /**
     * Clear all items in wishlist
     */
    @Transactional
    public void clearWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        wishlistRepository.deleteAllByUserId(user.getId());
    }
}
