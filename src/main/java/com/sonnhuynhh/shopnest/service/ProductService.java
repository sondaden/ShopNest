// src/main/java/com/sonnhuynhh/shopnest/service/ProductService.java
package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.ProductRequest;
import com.sonnhuynhh.shopnest.model.Brand;
import com.sonnhuynhh.shopnest.model.Category;
import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.model.ProductImage;
import com.sonnhuynhh.shopnest.repository.BrandRepository;
import com.sonnhuynhh.shopnest.repository.CategoryRepository;
import com.sonnhuynhh.shopnest.repository.ProductImageRepository;
import com.sonnhuynhh.shopnest.repository.ProductRepository;
import com.sonnhuynhh.shopnest.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Transactional
    public Product addProduct(ProductRequest request) {
        Product product = new Product();
        mapRequestToProduct(request, product);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        
        // Lưu các ảnh
        saveProductImages(savedProduct, request.imageUrls());
        
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        mapRequestToProduct(request, product);
        product.setUpdatedAt(LocalDateTime.now());
        
        // Xóa ảnh cũ và thêm ảnh mới nếu có
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            // Xóa ảnh cũ
            product.getImages().clear();
            productRepository.save(product);
            
            // Thêm ảnh mới
            saveProductImages(product, request.imageUrls());
        }
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
    
    /**
     * Lưu danh sách ảnh cho sản phẩm
     */
    private void saveProductImages(Product product, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i);
            if (url != null && !url.trim().isEmpty()) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(url.trim());
                image.setDisplayOrder(i);
                image.setIsPrimary(i == 0); // Ảnh đầu tiên là ảnh chính
                productImageRepository.save(image);
                
                // Cập nhật imageUrl của product nếu là ảnh đầu tiên
                if (i == 0) {
                    product.setImageUrl(url.trim());
                    productRepository.save(product);
                }
            }
        }
    }
    
    /**
     * Lấy danh sách ảnh của sản phẩm
     */
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    }
    
    /**
     * Thêm ảnh cho sản phẩm
     */
    @Transactional
    public ProductImage addProductImage(Long productId, String imageUrl, boolean isPrimary) {
        Product product = getProductById(productId);
        
        // Nếu đặt làm ảnh chính, bỏ ảnh chính cũ
        if (isPrimary) {
            product.getImages().forEach(img -> img.setIsPrimary(false));
            product.setImageUrl(imageUrl);
            productRepository.save(product);
        }
        
        int order = productImageRepository.countByProductId(productId);
        ProductImage image = new ProductImage(product, imageUrl, order, isPrimary);
        return productImageRepository.save(image);
    }
    
    /**
     * Xóa ảnh của sản phẩm
     */
    @Transactional
    public void deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        
        // Nếu xóa ảnh chính, cập nhật ảnh chính mới
        if (Boolean.TRUE.equals(image.getIsPrimary())) {
            Product product = image.getProduct();
            productImageRepository.delete(image);
            
            // Đặt ảnh đầu tiên còn lại làm ảnh chính
            List<ProductImage> remainingImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
            if (!remainingImages.isEmpty()) {
                ProductImage newPrimary = remainingImages.get(0);
                newPrimary.setIsPrimary(true);
                product.setImageUrl(newPrimary.getImageUrl());
            } else {
                product.setImageUrl(null);
            }
            productRepository.save(product);
        } else {
            productImageRepository.delete(image);
        }
    }

    private void mapRequestToProduct(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setSlug(SlugUtils.toSlug(request.name()));
        product.setPrice(request.price() != null ? request.price() : BigDecimal.ZERO);
        product.setStock(request.stock() != null ? request.stock() : 0);
        product.setDescription(request.description());
        
        // Chỉ cập nhật imageUrl nếu không có danh sách ảnh
        if (request.imageUrls() == null || request.imageUrls().isEmpty()) {
            product.setImageUrl(request.imageUrl());
        }
        
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