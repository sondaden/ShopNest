package com.sonnhuynhh.shopnest.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test để xác minh fix lỗi "double ROLE_ prefix" trong JwtService.
 * 
 * BUG: JwtService.generateToken() thêm "ROLE_" prefix vào authority,
 * nhưng UserDetailsServiceImpl đã thêm "ROLE_" prefix rồi.
 * Kết quả: JWT token chứa "ROLE_ROLE_ADMIN" thay vì "ROLE_ADMIN".
 * 
 * FIX: Loại bỏ việc thêm "ROLE_" trong JwtService vì authority đã có prefix.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGtleSBmb3IgdGVzdGluZyBwdXJwb3NlcyBvbmx5"; // Base64 encoded
    private static final long EXPIRATION = 86400000; // 1 day

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    /**
     * Test case chứng minh bug đã được fix:
     * Khi authority là "ROLE_ADMIN", JWT token phải chứa "ROLE_ADMIN",
     * KHÔNG PHẢI "ROLE_ROLE_ADMIN".
     */
    @Test
    void generateToken_shouldNotDoubleRolePrefix() {
        // Given: UserDetails với authority đã có prefix "ROLE_" 
        // (giống như UserDetailsServiceImpl tạo ra)
        UserDetails userDetails = new User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When: Generate JWT token
        String token = jwtService.generateToken(userDetails);

        // Then: Extract role claim và verify không có double prefix
        Claims claims = extractClaims(token);
        String roleClaim = claims.get("role", String.class);

        // QUAN TRỌNG: Role phải là "ROLE_ADMIN", KHÔNG PHẢI "ROLE_ROLE_ADMIN"
        assertEquals("ROLE_ADMIN", roleClaim, 
                "Role claim should be 'ROLE_ADMIN', not 'ROLE_ROLE_ADMIN'. " +
                "Double prefix bug detected if this fails.");
        
        // Verify không chứa "ROLE_ROLE_"
        assertFalse(roleClaim.contains("ROLE_ROLE_"), 
                "Role claim should not contain double 'ROLE_' prefix");
    }

    /**
     * Test case cho USER role.
     */
    @Test
    void generateToken_shouldNotDoubleRolePrefixForUser() {
        // Given: UserDetails với ROLE_USER
        UserDetails userDetails = new User(
                "normaluser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When: Generate JWT token
        String token = jwtService.generateToken(userDetails);

        // Then: Role phải là "ROLE_USER", KHÔNG PHẢI "ROLE_ROLE_USER"
        Claims claims = extractClaims(token);
        String roleClaim = claims.get("role", String.class);

        assertEquals("ROLE_USER", roleClaim,
                "Role claim should be 'ROLE_USER', not 'ROLE_ROLE_USER'");
    }

    /**
     * Test username được lưu đúng trong token.
     */
    @Test
    void generateToken_shouldContainCorrectUsername() {
        UserDetails userDetails = new User(
                "testuser@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);

        assertEquals("testuser@example.com", extractedUsername);
    }

    /**
     * Test token validity.
     */
    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        UserDetails userDetails = new User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    private Claims extractClaims(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
