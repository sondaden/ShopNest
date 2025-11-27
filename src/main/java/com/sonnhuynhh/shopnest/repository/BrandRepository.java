// src/main/java/com/sonnhuynhh/shopnest/repository/BrandRepository.java
package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);
    boolean existsByName(String name);
}