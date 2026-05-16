package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // TTL 7 days in seconds
    private static final long REFRESH_TTL = 7 * 24 * 60 * 60;

    // Store refresh token in Redis
    public void storeRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
            "refresh:" + userId,
            refreshToken,
            REFRESH_TTL,
            TimeUnit.SECONDS
        );
    }

    // Validate refresh token
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String stored = redisTemplate.opsForValue()
            .get("refresh:" + userId);
        return refreshToken.equals(stored);
    }

    // Delete refresh token (logout)
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    // Blacklist access token (logout)
    public void blacklistAccessToken(String token, long expirySeconds) {
        redisTemplate.opsForValue().set(
            "blacklist:" + token,
            "true",
            expirySeconds,
            TimeUnit.SECONDS
        );
    }

    // Check if access token is blacklisted
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
            redisTemplate.hasKey("blacklist:" + token)
        );
    }
}