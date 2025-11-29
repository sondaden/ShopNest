package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.ProductRatingResponse;
import com.sonnhuynhh.shopnest.dto.ReviewRequest;
import com.sonnhuynhh.shopnest.dto.ReviewResponse;
import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.model.Review;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.ProductRepository;
import com.sonnhuynhh.shopnest.repository.ReviewRepository;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ReviewResponse addReview(ReviewRequest request) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        // Check if user already reviewed this product
        if (reviewRepository.existsByUserAndProduct(currentUser, product)) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }
        
        Review review = Review.builder()
                .user(currentUser)
                .product(product)
                .rating(request.rating())
                .comment(request.comment())
                .build();
        
        review = reviewRepository.save(review);
        
        // Update product average rating
        updateProductRating(product);
        
        return mapToResponse(review, currentUser.getId());
    }
    
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        User currentUser = getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
        }
        
        review.setRating(request.rating());
        review.setComment(request.comment());
        review = reviewRepository.save(review);
        
        // Update product average rating
        updateProductRating(review.getProduct());
        
        return mapToResponse(review, currentUser.getId());
    }
    
    @Transactional
    public void deleteReview(Long reviewId) {
        User currentUser = getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        // Allow owner or admin to delete
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        if (!review.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
        }
        
        Product product = review.getProduct();
        reviewRepository.delete(review);
        
        // Update product average rating
        updateProductRating(product);
    }
    
    public List<ReviewResponse> getProductReviews(Long productId) {
        Long currentUserId = getCurrentUserIdOrNull();
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(review -> mapToResponse(review, currentUserId))
                .toList();
    }
    
    public List<ReviewResponse> getUserReviews() {
        User currentUser = getCurrentUser();
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(review -> mapToResponse(review, currentUser.getId()))
                .toList();
    }
    
    public ProductRatingResponse getProductRating(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countByProductId(productId);
        
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        
        List<Object[]> ratingDist = reviewRepository.getRatingDistributionByProductId(productId);
        for (Object[] row : ratingDist) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count);
        }
        
        return new ProductRatingResponse(
                productId,
                avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
                totalReviews,
                distribution
        );
    }
    
    public boolean hasUserReviewed(Long productId) {
        try {
            User currentUser = getCurrentUser();
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) return false;
            return reviewRepository.existsByUserAndProduct(currentUser, product);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void updateProductRating(Product product) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
        product.setRating(avgRating != null ? avgRating : 0.0);
        productRepository.save(product);
    }
    
    private ReviewResponse mapToResponse(Review review, Long currentUserId) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getUser().getId(),
                review.getUser().getFullName(),
                review.getUser().getAvatarUrl(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                currentUserId != null && currentUserId.equals(review.getUser().getId())
        );
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Vui lòng đăng nhập");
        }
        
        String email;
        Object principal = auth.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            email = userDetails.getUsername();
        } else {
            email = auth.getName();
        }
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }
    
    private Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }
}
