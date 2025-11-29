package com.sonnhuynhh.shopnest.dto;

import com.sonnhuynhh.shopnest.model.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WishlistItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer discountPercent;
    private Boolean inStock;
    private String categoryName;
    private LocalDateTime addedAt;
    
    public static WishlistItemResponse fromProduct(Product product, LocalDateTime addedAt) {
        BigDecimal price = product.getPrice();
        
        return WishlistItemResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .imageUrl(product.getImageUrl())
                .price(price)
                .originalPrice(null)
                .discountPercent(null)
                .inStock(product.getStock() > 0)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .addedAt(addedAt)
                .build();
    }
}
