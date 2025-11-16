package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product getProductsById(long id);
}
