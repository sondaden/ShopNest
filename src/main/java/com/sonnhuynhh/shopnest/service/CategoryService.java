// src/main/java/com/sonnhuynhh/shopnest/service/CategoryService.java
package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.CategoryRequest;
import com.sonnhuynhh.shopnest.model.Category;
import com.sonnhuynhh.shopnest.repository.CategoryRepository;
import com.sonnhuynhh.shopnest.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repo;

    public Category create(CategoryRequest req) {
        if (repo.existsByName(req.name())) throw new RuntimeException("Category name already exists");
        Category cat = new Category();
        cat.setName(req.name());
        cat.setSlug(SlugUtils.toSlug(req.name()));
        cat.setDescription(req.description());
        return repo.save(cat);
    }

    public java.util.List<Category> getAll() { return repo.findAll(); }
    public Category getById(Long id) { return repo.findById(id).orElseThrow(); }
    // update, delete tự thêm tương tự
}