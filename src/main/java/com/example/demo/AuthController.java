package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @RequestBody AuthRequest request) {

        Object result = userService.register(request);

        if (result.equals("EMAIL_EXISTS"))
            return ResponseEntity.status(409)
                .body("❌ Email already registered!");

        if (result.equals("NAME_REQUIRED"))
            return ResponseEntity.status(400)
                .body("❌ Name is required!");

        if (result.equals("ROLE_REQUIRED"))
            return ResponseEntity.status(400)
                .body("❌ Role is required! (DONOR/NGO/PROVIDER/ADMIN)");

        User user = (User) result;

        String accessToken = jwtUtil.generateAccessToken(
            user.getId(), user.getName(), user.getRole().toString());

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Store refresh token in Redis
        refreshTokenService.storeRefreshToken(user.getId(), refreshToken);

        return ResponseEntity.status(201).body(
            new AuthResponse(accessToken, refreshToken,
                user.getId(), user.getName(),
                user.getEmail(), user.getRole().toString()));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @RequestBody AuthRequest request) {

        Object result = userService.authenticate(
            request.getId(), request.getPassword());

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ No record found for this ID!");

        if (result.equals("WRONG_PASSWORD"))
            return ResponseEntity.status(401)
                .body("⚠️ Wrong password! Please re-enter.");

        User user = (User) result;

        String accessToken = jwtUtil.generateAccessToken(
            user.getId(), user.getName(), user.getRole().toString());

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Store refresh token in Redis
        refreshTokenService.storeRefreshToken(user.getId(), refreshToken);

        return ResponseEntity.ok(
            new AuthResponse(accessToken, refreshToken,
                user.getId(), user.getName(),
                user.getEmail(), user.getRole().toString()));
    }

    // POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(
            @RequestBody java.util.Map<String, String> body) {

        String refreshToken = body.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ Refresh token is required!");

        if (!jwtUtil.validateToken(refreshToken))
            return ResponseEntity.status(401)
                .body("❌ Invalid or expired refresh token!");

        Long userId = jwtUtil.extractUserId(refreshToken);

        // Validate against Redis
        if (!refreshTokenService.validateRefreshToken(userId, refreshToken))
            return ResponseEntity.status(401)
                .body("❌ Refresh token mismatch! Please login again.");

        // Get user details
        java.util.Optional<User> optUser =
            userService.findById(userId);

        if (optUser.isEmpty())
            return ResponseEntity.status(404)
                .body("❌ User not found!");

        User user = optUser.get();

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(
            user.getId(), user.getName(), user.getRole().toString());

        // Generate new refresh token
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Update Redis with new refresh token
        refreshTokenService.storeRefreshToken(user.getId(), newRefreshToken);

        return ResponseEntity.ok(java.util.Map.of(
            "accessToken", newAccessToken,
            "refreshToken", newRefreshToken
        ));
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(400)
                .body("❌ Authorization header missing!");

        String accessToken = authHeader.substring(7);

        if (!jwtUtil.validateToken(accessToken))
            return ResponseEntity.status(401)
                .body("❌ Invalid token!");

        Long userId = jwtUtil.extractUserId(accessToken);

        // Blacklist access token in Redis
        long expiry = jwtUtil.extractAllClaims(accessToken)
            .getExpiration().getTime() - System.currentTimeMillis();

        refreshTokenService.blacklistAccessToken(
            accessToken, expiry / 1000);

        // Delete refresh token from Redis
        refreshTokenService.deleteRefreshToken(userId);

        return ResponseEntity.ok("✅ Logged out successfully!");
    }

    @GetMapping("/test-bcrypt")
    public String testBcrypt() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode("password123");
    }
}