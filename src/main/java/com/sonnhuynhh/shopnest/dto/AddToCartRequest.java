// src/main/java/com/sonnhuynhh/shopnest/dto/AddToCartRequest.java
package com.sonnhuynhh.shopnest.dto;

public record AddToCartRequest(Long productId, int quantity) {
}
