// src/main/java/com/sonnhuynhh/shopnest/controller/SearchController.java
package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.SearchRequest;
import com.sonnhuynhh.shopnest.dto.SearchResponse;
import com.sonnhuynhh.shopnest.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(@ModelAttribute SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> suggestions(@RequestParam String q) {
        return ResponseEntity.ok(searchService.getSuggestions(q));
    }
}