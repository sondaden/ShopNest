package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.repository.ProductRepository;
import com.sonnhuynhh.shopnest.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    // cung cấp instance tự động
    @Autowired
    // inject ProductRepository
    private ProductRepository productRepository;
    private SupabaseService supabaseService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setPrice(product.getPrice());
                    existingProduct.setStock(product.getStock());
                    return ResponseEntity.ok(productRepository.save(existingProduct));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }

    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody Map<String, Object> productData) {
        return supabaseService.insertProduct(productData);
    }

    // Thêm vào cuối class ProductController
    @GetMapping("/test-connection")
    public ResponseEntity<String> testSupabaseConnection() {
        return supabaseService.testConnection();
    }
}
