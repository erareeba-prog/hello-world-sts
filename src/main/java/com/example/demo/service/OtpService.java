package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long OTP_TTL = 5;

    public String generateOtp(Long userId, String phone) {
        String otp = String.format("%06d",
            new Random().nextInt(999999));
        String key = "otp:" + userId + ":" + phone;
        redisTemplate.opsForValue().set(
            key, otp, OTP_TTL, TimeUnit.MINUTES
        );
        return otp;
    }

    public boolean validateOtp(Long userId,
                                String phone,
                                String otp) {
        String key = "otp:" + userId + ":" + phone;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) return false;
        if (stored.equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    public boolean otpExists(Long userId, String phone) {
        String key = "otp:" + userId + ":" + phone;
        return Boolean.TRUE.equals(
            redisTemplate.hasKey(key)
        );
    }

    public Long getOtpTtl(Long userId, String phone) {
        String key = "otp:" + userId + ":" + phone;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}