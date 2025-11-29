package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.Wishlist;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    List<Wishlist> findByUserOrderByCreatedAtDesc(User user);
    
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<Wishlist> findByUserAndProduct(User user, Product product);
    
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);
    
    boolean existsByUserAndProduct(User user, Product product);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.product.id = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
    
    long countByUserId(Long userId);
    
    @Query("SELECT w.product.id FROM Wishlist w WHERE w.user.id = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);
}
