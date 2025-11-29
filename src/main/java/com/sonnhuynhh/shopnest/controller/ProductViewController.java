package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.SearchRequest;
import com.sonnhuynhh.shopnest.dto.SearchResponse;
import com.sonnhuynhh.shopnest.dto.SortOption;
import com.sonnhuynhh.shopnest.model.Brand;
import com.sonnhuynhh.shopnest.model.Category;
import com.sonnhuynhh.shopnest.model.Product;
import com.sonnhuynhh.shopnest.repository.BrandRepository;
import com.sonnhuynhh.shopnest.service.CategoryService;
import com.sonnhuynhh.shopnest.service.ProductService;
import com.sonnhuynhh.shopnest.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller xử lý các trang sản phẩm (Thymeleaf views)
 * Sử dụng SearchService để đảm bảo logic giống với API /api/search
 */
@Controller
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ProductViewController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandRepository brandRepository;
    private final SearchService searchService;

    /**
     * Trang danh sách sản phẩm - sử dụng SearchService (cùng logic với API)
     */
    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "minPrice", required = false) String minPriceStr,
            @RequestParam(name = "maxPrice", required = false) String maxPriceStr,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            Model model) {
        
        // Load categories và brands cho bộ lọc - luôn load trước
        List<Category> categories = categoryService.getAll();
        List<Brand> brands = brandRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("sort", sort);
        
        // Parse price strings to BigDecimal (handle empty strings)
        BigDecimal minPrice = parsePrice(minPriceStr);
        BigDecimal maxPrice = parsePrice(maxPriceStr);
        
        try {
            
            // Map sort string to SortOption enum
            SortOption sortOption = mapSortOption(sort);
            
            // Tạo SearchRequest và gọi SearchService (cùng logic với API /api/search)
            SearchRequest searchRequest = new SearchRequest(
                    keyword,
                    page,
                    size,
                    categoryId,
                    null, // category name
                    brandId,
                    null, // brand name
                    minPrice,
                    maxPrice,
                    sortOption
            );
            
            SearchResponse searchResponse = searchService.search(searchRequest);
            
            // Thêm kết quả vào model
            model.addAttribute("products", searchResponse.items());
            model.addAttribute("totalProducts", searchResponse.total());
            model.addAttribute("currentPage", searchResponse.page());
            model.addAttribute("totalPages", (int) Math.ceil((double) searchResponse.total() / size));
            model.addAttribute("suggestions", searchResponse.suggestions());
            
            // Giữ lại các giá trị filter đã chọn
            if (categoryId != null) {
                model.addAttribute("selectedCategoryId", categoryId);
                categories.stream()
                        .filter(c -> c.getId().equals(categoryId))
                        .findFirst()
                        .ifPresent(c -> model.addAttribute("selectedCategoryName", c.getName()));
            }
            if (brandId != null) {
                model.addAttribute("selectedBrandId", brandId);
                brands.stream()
                        .filter(b -> b.getId().equals(brandId))
                        .findFirst()
                        .ifPresent(b -> model.addAttribute("selectedBrandName", b.getName()));
            }
            if (keyword != null && !keyword.isBlank()) {
                model.addAttribute("keyword", keyword);
                // Log search để tạo suggestions
                searchService.logSearch(keyword);
            }
            if (minPrice != null) model.addAttribute("minPrice", minPrice);
            if (maxPrice != null) model.addAttribute("maxPrice", maxPrice);
            if (rating != null) model.addAttribute("selectedRating", rating);
            
        } catch (Exception e) {
            log.error("Error loading products: {}", e.getMessage(), e);
            model.addAttribute("error", "Không thể tải danh sách sản phẩm: " + e.getMessage());
        }
        return "products";
    }
    
    private SortOption mapSortOption(String sort) {
        return switch (sort) {
            case "price-asc" -> SortOption.PRICE_ASC;
            case "price-desc" -> SortOption.PRICE_DESC;
            case "rating" -> SortOption.RATING_DESC;
            default -> SortOption.NEWEST;
        };
    }
    
    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(priceStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Trang chi tiết sản phẩm
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        try {
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);
            
            // Lấy sản phẩm liên quan (cùng category)
            if (product.getCategory() != null) {
                List<Product> relatedProducts = productService.getAllProducts().stream()
                        .filter(p -> p.getCategory() != null 
                                && p.getCategory().getId().equals(product.getCategory().getId())
                                && !p.getId().equals(id))
                        .limit(4)
                        .toList();
                model.addAttribute("relatedProducts", relatedProducts);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy sản phẩm: " + e.getMessage());
            return "error";
        }
        return "product-detail";
    }
}
