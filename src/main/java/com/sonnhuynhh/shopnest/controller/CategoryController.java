// src/main/java/com/sonnhuynhh/shopnest/controller/CategoryController.java
package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.CategoryRequest;
import com.sonnhuynhh.shopnest.model.Category;
import com.sonnhuynhh.shopnest.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {
    private final CategoryService service;

    @GetMapping
    public List<Category> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public Category create(@RequestBody CategoryRequest req) { return service.create(req); }
}