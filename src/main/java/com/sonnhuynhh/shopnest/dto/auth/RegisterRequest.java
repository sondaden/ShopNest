// src/main/java/com/sonnhuynhh/shopnest/dto/auth/RegisterRequest.java
package com.sonnhuynhh.shopnest.dto.auth;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String fullName,
        String phone
) {}