// src/main/java/com/sonnhuynhh/shopnest/service/CartService.java
package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.AddToCartRequest;
import com.sonnhuynhh.shopnest.dto.CartItemResponse;
import com.sonnhuynhh.shopnest.dto.CartResponse;
import com.sonnhuynhh.shopnest.model.*;
import com.sonnhuynhh.shopnest.repository.CartItemRepository;
import com.sonnhuynhh.shopnest.repository.CartRepository;
import com.sonnhuynhh.shopnest.repository.ProductRepository;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;  // ← THÊM DÒNG NÀY

    // LẤY USER HIỆN TẠI KHÔNG CẦN UserService
    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public CartResponse getCart() {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createCartForUser(user));
        return mapToCartResponse(cart);
    }

    public CartResponse addToCart(AddToCartRequest request) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createCartForUser(user));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.quantity()),
                        () -> {
                            CartItem newItem = new CartItem();
                            newItem.setCart(cart);
                            newItem.setProduct(product);
                            newItem.setQuantity(request.quantity());
                            cart.getItems().add(newItem);
                        }
                );

        return mapToCartResponse(cart);
    }

    public CartResponse removeFromCart(Long productId) {
        User user = getCurrentUser();
        Cart cart = getUserCart(user);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        return mapToCartResponse(cart);
    }

    public CartResponse removeByItemId(Long itemId) {
        User user = getCurrentUser();
        Cart cart = getUserCart(user);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return mapToCartResponse(cart);
    }

    public CartResponse updateQuantity(Long itemId, Integer quantity) {
        User user = getCurrentUser();
        Cart cart = getUserCart(user);
        
        cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        cart.getItems().remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                });
        
        return mapToCartResponse(cart);
    }

    public void clearCart() {
        User user = getCurrentUser();
        cartRepository.findByUserId(user.getId())
                .ifPresent(cart -> cart.getItems().clear());
    }

    private Cart createCartForUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private Cart getUserCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, items.size(), totalPrice);
    }
}