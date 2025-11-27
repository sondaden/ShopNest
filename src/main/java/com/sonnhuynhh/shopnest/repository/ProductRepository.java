package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
//    Product getProductsById(long id);

    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Find by slug for SEO-friendly URLs
    Optional<Product> findBySlug(String slug);
    
    // Find related products by category (excluding current product)
    List<Product> findByCategoryAndIdNot(String category, Long id);
    
    // Find by category
    List<Product> findByCategory(String category);
    
    // Find top products by category (limited)
    List<Product> findTop4ByCategoryAndIdNotOrderByRatingDesc(String category, Long id);
}