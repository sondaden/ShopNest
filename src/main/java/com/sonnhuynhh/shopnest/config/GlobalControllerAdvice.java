package com.sonnhuynhh.shopnest.config;

import com.sonnhuynhh.shopnest.dto.CartResponse;
import com.sonnhuynhh.shopnest.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to add common model attributes to all views
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CartService cartService;

    /**
     * Add cart count to all views for the navbar badge
     */
    @ModelAttribute("cartCount")
    public Integer getCartCount() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                CartResponse cart = cartService.getCart();
                return cart.totalItems();
            }
        } catch (Exception e) {
            // Ignore errors - just return 0
        }
        return 0;
    }
}
