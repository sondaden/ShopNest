// src/main/java/com/sonnhuynhh/shopnest/dto/auth/AuthResponse.java
package com.sonnhuynhh.shopnest.dto.auth;

public record AuthResponse(String token, String username, String role) {}