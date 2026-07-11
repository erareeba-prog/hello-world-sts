package com.example.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET =
        "mySecretKey12345mySecretKey12345";

    // Access token expires in 1 hour
    private static final long ACCESS_EXPIRATION =
        1000 * 60 * 60;

    // Refresh token expires in 7 days
    private static final long REFRESH_EXPIRATION =
        1000 * 60 * 60 * 24 * 7;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Generate Access Token
    public String generateAccessToken(Long userId, String name, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", name);
        claims.put("role", role);
        claims.put("type", "access");

        return Jwts.builder()
            .claims(claims)
            .subject(String.valueOf(userId))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
            .signWith(getSigningKey())
            .compact();
    }

    // Generate Refresh Token
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
            .claims(claims)
            .subject(String.valueOf(userId))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
            .signWith(getSigningKey())
            .compact();
    }

    // Validate Token
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract All Claims
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // Extract User ID
    public Long extractUserId(String token) {
        return Long.parseLong(
            extractAllClaims(token).getSubject()
        );
    }

    // Extract Name
    public String extractName(String token) {
        return extractAllClaims(token)
            .get("name", String.class);
    }

    // Extract Role
    public String extractRole(String token) {
        return extractAllClaims(token)
            .get("role", String.class);
    }

    // Check if Expired
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token)
            .getExpiration()
            .before(new Date());
    }
}