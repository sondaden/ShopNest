package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.Category;
import com.sonnhuynhh.shopnest.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
//    Product getProductsById(long id);

    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Find by slug for SEO-friendly URLs
    Optional<Product> findBySlug(String slug);
    
    // Find related products by category (excluding current product)
    List<Product> findByCategoryAndIdNot(Category category, Long id);
    
    // Find by category
    List<Product> findByCategory(Category category);
    
    // Find top products by category (limited)
    List<Product> findTop4ByCategoryAndIdNotOrderByRatingDesc(Category category, Long id);
}