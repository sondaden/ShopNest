package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.*;
import com.sonnhuynhh.shopnest.model.*;
import com.sonnhuynhh.shopnest.repository.*;
import com.sonnhuynhh.shopnest.utils.VietnameseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductRepository productRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_SUGGESTIONS = "search:suggestions";
    private static final long CACHE_TTL = 300; // 5 phút

    private User getCurrentUserOrNull() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) return null;
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // TÌM KIẾM CHỈ ĐỌC → readOnly = true
    @Transactional(readOnly = true)
    public SearchResponse search(SearchRequest request) {
        String keyword = request.q() != null ? request.q().trim() : null;

        Pageable pageable = PageRequest.of(request.page(), request.size(), request.sort().getSort());

        Specification<Product> spec = null;

        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            String noAccentKeyword = VietnameseUtils.removeAccent(keyword).toLowerCase();

            Specification<Product> keywordSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + lowerKeyword + "%"),
                    cb.like(cb.lower(root.get("slug")), "%" + lowerKeyword + "%"),
                    cb.like(cb.lower(root.get("name")), "%" + noAccentKeyword + "%"),
                    cb.like(cb.lower(root.get("slug")), "%" + noAccentKeyword + "%")
            );
            spec = spec == null ? keywordSpec : spec.and(keywordSpec);
        }

        if (request.categoryId() != null) {
            Specification<Product> catSpec = (root, query, cb) -> cb.equal(root.get("category").get("id"), request.categoryId());
            spec = spec == null ? catSpec : spec.and(catSpec);
        }
        if (request.brandId() != null) {
            Specification<Product> brandSpec = (root, query, cb) -> cb.equal(root.get("brand").get("id"), request.brandId());
            spec = spec == null ? brandSpec : spec.and(brandSpec);
        }
        if (request.minPrice() != null) {
            Specification<Product> minSpec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), request.minPrice());
            spec = spec == null ? minSpec : spec.and(minSpec);
        }
        if (request.maxPrice() != null) {
            Specification<Product> maxSpec = (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), request.maxPrice());
            spec = spec == null ? maxSpec : spec.and(maxSpec);
        }

        Page<Product> page = productRepository.findAll(spec, pageable);

        List<ProductSearchDto> items = page.getContent().stream()
                .map(p -> new ProductSearchDto(
                        p.getId(),
                        p.getName(),
                        p.getSlug(),
                        p.getImageUrl(),
                        p.getPrice(),
                        p.getRating(),
                        p.getStock(),
                        p.getCategory() != null ? p.getCategory().getId() : null,
                        p.getCategory() != null ? p.getCategory().getName() : null,
                        p.getBrand() != null ? p.getBrand().getId() : null,
                        p.getBrand() != null ? p.getBrand().getName() : null
                ))
                .toList();

        List<String> suggestions = (keyword != null && keyword.length() >= 2)
                ? getSuggestions(keyword)
                : Collections.emptyList();

        return new SearchResponse(items, page.getTotalElements(), page.getNumber(), page.getSize(), suggestions);
    }

    // GỢI Ý – CHỈ ĐỌC
    @Transactional(readOnly = true)
    @Cacheable(value = "suggestions", key = "#prefix")
    public List<String> getSuggestions(String prefix) {
        if (prefix == null || prefix.trim().length() < 2) return List.of();

        String cacheKey = CACHE_SUGGESTIONS + ":" + prefix.toLowerCase().trim();
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof String) {
            @SuppressWarnings("unchecked")
            List<String> ret = (List<String>) list;
            return ret;
        }

        List<String> suggestions = searchHistoryRepository
                .findTop10ByKeywordStartingWithIgnoreCaseOrderBySearchCountDesc(prefix.trim())
                .stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .limit(10)
                .toList();

        redisTemplate.opsForValue().set(cacheKey, suggestions, CACHE_TTL, TimeUnit.SECONDS);
        return suggestions;
    }

    // LƯU LỊCH SỬ TÌM KIẾM – GỌI RIÊNG, KHÔNG GỌI TRONG SEARCH()
    @Transactional
    public void logSearch(String keyword) {
        if (keyword == null || keyword.trim().isBlank()) return;

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

        // Xóa cache gợi ý
        Set<String> keys = redisTemplate.keys(CACHE_SUGGESTIONS + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
