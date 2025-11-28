// src/main/java/com/sonnhuynhh/shopnest/service/ProductService.java
package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.ProductRequest;
import com.sonnhuynhh.shopnest.model.Brand;
import com.sonnhuynhh.shopnest.model.Category;
import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.repository.BrandRepository;
import com.sonnhuynhh.shopnest.repository.CategoryRepository;
import com.sonnhuynhh.shopnest.repository.ProductRepository;
import com.sonnhuynhh.shopnest.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product addProduct(ProductRequest request) {
        Product product = new Product();
        mapRequestToProduct(request, product);  // ← Gọi method chung
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        mapRequestToProduct(request, product);  // ← Dùng lại 100%
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private void mapRequestToProduct(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setSlug(SlugUtils.toSlug(request.name()));
        product.setPrice(request.price() != null ? request.price() : BigDecimal.ZERO);
        product.setStock(request.stock() != null ? request.stock() : 0);
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setRating(request.rating() != null ? request.rating() : 0.0);

        // Xử lý Category
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.categoryId()));
        product.setCategory(category);

        // Xử lý Brand (có thể null)
        Brand brand = null;
        if (request.brandId() != null) {
            brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + request.brandId()));
        }
        product.setBrand(brand);
    }
}