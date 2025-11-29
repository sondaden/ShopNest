package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.*;
import com.sonnhuynhh.shopnest.model.*;
import com.sonnhuynhh.shopnest.repository.*;
import com.sonnhuynhh.shopnest.utils.VietnameseUtils;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class SearchService {

    private final ProductRepository productRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_SUGGESTIONS = "search:suggestions";
    private static final long CACHE_TTL = 300;

    private User getCurrentUserOrNull() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) return null;
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String normalize(String input) {
        if (input == null || input.isBlank()) return null;
        return VietnameseUtils.removeAccent(input)
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    @Transactional(readOnly = true)
    public SearchResponse search(SearchRequest request) {
        String keyword = request.q() != null ? request.q().trim() : null;
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        Pageable pageable = PageRequest.of(request.page(), request.size(), request.sort().getSort());

        // Bắt đầu với spec = null, sẽ build dần
        Specification<Product> spec = Specification.where(null);

        // 1. Lọc theo categoryId
        if (request.categoryId() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("category").get("id"), request.categoryId()));
        }

        // 2. Lọc theo category name (nếu có)
        if (request.category() != null && !request.category().isBlank()) {
            String catName = request.category().toLowerCase();
            spec = spec.and((root, query, cb) -> 
                    cb.like(cb.lower(root.get("category").get("name")), "%" + catName + "%"));
        }

        // 3. Lọc theo brandId
        if (request.brandId() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("brand").get("id"), request.brandId()));
        }

        // 4. Lọc theo brand name (nếu có)
        if (request.brand() != null && !request.brand().isBlank()) {
            String brandName = request.brand().toLowerCase();
            spec = spec.and((root, query, cb) -> 
                    cb.like(cb.lower(root.get("brand").get("name")), "%" + brandName + "%"));
        }

        // 5. Lọc giá
        if (request.minPrice() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), request.minPrice()));
        }
        if (request.maxPrice() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), request.maxPrice()));
        }

        // 6. Tìm kiếm theo keyword (nếu có)
        if (hasKeyword) {
            String lower = keyword.toLowerCase();
            String noAccent = VietnameseUtils.removeAccent(keyword).toLowerCase();
            String noSpaceNoAccent = noAccent.replaceAll("\\s+", "");

            Specification<Product> keywordSpec = (root, query, cb) -> {
                List<Predicate> searchPredicates = new ArrayList<>();

                // Tìm trong tên + slug sản phẩm (có và không có khoảng cách)
                searchPredicates.add(cb.like(cb.lower(root.get("name")), "%" + lower + "%"));
                searchPredicates.add(cb.like(cb.lower(root.get("slug")), "%" + lower + "%"));
                searchPredicates.add(cb.like(cb.lower(root.get("name")), "%" + noAccent + "%"));
                searchPredicates.add(cb.like(cb.lower(root.get("slug")), "%" + noAccent + "%"));

                // So sánh không khoảng cách: loại bỏ space khỏi name/slug
                searchPredicates.add(cb.like(
                        cb.lower(cb.function("replace", String.class, root.get("name"), cb.literal(" "), cb.literal(""))),
                        "%" + noSpaceNoAccent + "%"
                ));
                searchPredicates.add(cb.like(
                        cb.lower(cb.function("replace", String.class, root.get("slug"), cb.literal(" "), cb.literal(""))),
                        "%" + noSpaceNoAccent + "%"
                ));

                // Tìm trong tên + slug danh mục
                var categoryJoin = root.join("category", JoinType.LEFT);
                searchPredicates.add(cb.like(cb.lower(categoryJoin.get("name")), "%" + lower + "%"));
                searchPredicates.add(cb.like(cb.lower(categoryJoin.get("slug")), "%" + lower + "%"));
                searchPredicates.add(cb.like(cb.lower(categoryJoin.get("name")), "%" + noAccent + "%"));
                searchPredicates.add(cb.like(cb.lower(categoryJoin.get("slug")), "%" + noAccent + "%"));

                searchPredicates.add(cb.like(
                        cb.lower(cb.function("replace", String.class, categoryJoin.get("name"), cb.literal(" "), cb.literal(""))),
                        "%" + noSpaceNoAccent + "%"
                ));
                searchPredicates.add(cb.like(
                        cb.lower(cb.function("replace", String.class, categoryJoin.get("slug"), cb.literal(" "), cb.literal(""))),
                        "%" + noSpaceNoAccent + "%"
                ));

                // Tìm trong tên + slug thương hiệu
                var brandJoin = root.join("brand", JoinType.LEFT);
                searchPredicates.add(cb.like(cb.lower(brandJoin.get("name")), "%" + lower + "%"));
                searchPredicates.add(cb.like(cb.lower(brandJoin.get("slug")), "%" + lower + "%"));
                searchPredicates.add(cb.like(cb.lower(brandJoin.get("name")), "%" + noAccent + "%"));
                searchPredicates.add(cb.like(cb.lower(brandJoin.get("slug")), "%" + noAccent + "%"));

                searchPredicates.add(cb.like(
                        cb.lower(cb.function("replace", String.class, brandJoin.get("name"), cb.literal(" "), cb.literal(""))),
                        "%" + noSpaceNoAccent + "%"
                ));
                searchPredicates.add(cb.like(
                        cb.lower(cb.function("replace", String.class, brandJoin.get("slug"), cb.literal(" "), cb.literal(""))),
                        "%" + noSpaceNoAccent + "%"
                ));

                return cb.or(searchPredicates.toArray(new Predicate[0]));
            };
            
            spec = spec.and(keywordSpec);
        }

        Page<Product> page = productRepository.findAll(spec, pageable);

        List<String> suggestions = hasKeyword && keyword.length() >= 2 
                ? getSuggestions(keyword) 
                : Collections.emptyList();

        return buildResponse(page, suggestions);
    }

    private SearchResponse buildResponse(Page<Product> page, List<String> suggestions) {
        List<ProductSearchDto> items = page.getContent().stream()
                .map(p -> new ProductSearchDto(
                        p.getId(), p.getName(), p.getSlug(), p.getImageUrl(),
                        p.getPrice(), p.getRating(), p.getStock(),
                        p.getCategory() != null ? p.getCategory().getId() : null,
                        p.getCategory() != null ? p.getCategory().getName() : null,
                        p.getBrand() != null ? p.getBrand().getId() : null,
                        p.getBrand() != null ? p.getBrand().getName() : null
                ))
                .toList();

        return new SearchResponse(items, page.getTotalElements(), page.getNumber(), page.getSize(), suggestions);
    }

    @Transactional(readOnly = true)
    public List<String> getSuggestions(String prefix) {
        if (prefix == null || prefix.trim().length() < 2) return List.of();

        // Try to get from Redis cache first
        try {
            String cacheKey = CACHE_SUGGESTIONS + ":" + prefix.toLowerCase().trim();
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof String) {
                @SuppressWarnings("unchecked")
                List<String> ret = (List<String>) list;
                return ret;
            }
        } catch (Exception e) {
            log.debug("Redis not available for suggestions cache: {}", e.getMessage());
        }

        // Get from database
        List<String> suggestions = searchHistoryRepository
                .findTop10ByKeywordStartingWithIgnoreCaseOrderBySearchCountDesc(prefix.trim())
                .stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .limit(10)
                .toList();

        // Try to cache in Redis
        try {
            String cacheKey = CACHE_SUGGESTIONS + ":" + prefix.toLowerCase().trim();
            redisTemplate.opsForValue().set(cacheKey, suggestions, CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("Redis not available for caching suggestions: {}", e.getMessage());
        }
        
        return suggestions;
    }

    @Transactional
    public void logSearch(String keyword) {
        if (keyword == null || keyword.trim().isBlank()) return;

        try {
            keyword = keyword.trim();
            User user = getCurrentUserOrNull();

            Optional<SearchHistory> existing = user != null
                    ? searchHistoryRepository.findByUserIdAndKeyword(user.getId(), keyword)
                    : searchHistoryRepository.findByUserIdIsNullAndKeyword(keyword);

            SearchHistory history = existing.orElse(new SearchHistory());
            if (!existing.isPresent()) {
                history.setUser(user);
                history.setKeyword(keyword);
                history.setSearchCount(1);
            } else {
                history.setSearchCount(history.getSearchCount() + 1);
            }
            history.setLastSearchedAt(LocalDateTime.now());
            searchHistoryRepository.save(history);

            // Try to clear Redis cache
            try {
                Set<String> keys = redisTemplate.keys(CACHE_SUGGESTIONS + ":*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                log.debug("Redis not available for cache invalidation: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to log search: {}", e.getMessage());
        }
    }
}
