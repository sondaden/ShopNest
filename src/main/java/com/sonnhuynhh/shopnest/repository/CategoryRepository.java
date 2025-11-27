// src/main/java/com/sonnhuynhh/shopnest/repository/CategoryRepository.java
package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
}