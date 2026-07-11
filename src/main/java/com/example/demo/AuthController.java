package com.example.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication",
     description = "Register, login, token refresh " +
         "and logout endpoints")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
        description = "Creates a new user account " +
            "and returns JWT tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "User registered successfully"),
        @ApiResponse(responseCode = "409",
            description = "Email already registered"),
        @ApiResponse(responseCode = "400",
            description = "Validation error")
    })
    public ResponseEntity<Object> register(
            @RequestBody AuthRequest request) {

        Object result = userService.register(request);

        if (result.equals("EMAIL_EXISTS"))
            return ResponseEntity.status(409)
                .body(com.example.demo.ApiResponse
                    .error("Email already registered!",
                        409));

        if (result.equals("NAME_REQUIRED"))
            return ResponseEntity.status(400)
                .body(com.example.demo.ApiResponse
                    .error("Name is required!", 400));

        if (result.equals("ROLE_REQUIRED"))
            return ResponseEntity.status(400)
                .body(com.example.demo.ApiResponse
                    .error("Role is required! " +
                        "(DONOR/NGO/PROVIDER/ADMIN)",
                        400));

        User user = (User) result;
        String accessToken = jwtUtil.generateAccessToken(
            user.getId(), user.getName(),
            user.getRole().toString());
        String refreshToken =
            jwtUtil.generateRefreshToken(user.getId());
        refreshTokenService.storeRefreshToken(
            user.getId(), refreshToken);

        return ResponseEntity.status(201).body(
            com.example.demo.ApiResponse.success(
                new AuthResponse(accessToken, refreshToken,
                    user.getId(), user.getName(),
                    user.getEmail(),
                    user.getRole().toString()),
                "User registered successfully!", 201));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user",
        description = "Authenticates user and " +
            "returns JWT access and refresh tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Login successful"),
        @ApiResponse(responseCode = "404",
            description = "User not found"),
        @ApiResponse(responseCode = "401",
            description = "Wrong password")
    })
    public ResponseEntity<Object> login(
            @RequestBody AuthRequest request) {

        Object result = userService.authenticate(
            request.getId(), request.getPassword());

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body(com.example.demo.ApiResponse
                    .error("No record found for this ID!",
                        404));

        if (result.equals("WRONG_PASSWORD"))
            return ResponseEntity.status(401)
                .body(com.example.demo.ApiResponse
                    .error("Wrong password! " +
                        "Please re-enter.", 401));

        User user = (User) result;
        String accessToken = jwtUtil.generateAccessToken(
            user.getId(), user.getName(),
            user.getRole().toString());
        String refreshToken =
            jwtUtil.generateRefreshToken(user.getId());
        refreshTokenService.storeRefreshToken(
            user.getId(), refreshToken);

        return ResponseEntity.ok(
            com.example.demo.ApiResponse.success(
                new AuthResponse(accessToken, refreshToken,
                    user.getId(), user.getName(),
                    user.getEmail(),
                    user.getRole().toString()),
                "Login successful!"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",
        description = "Issues a new access token " +
            "using a valid refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Token refreshed"),
        @ApiResponse(responseCode = "401",
            description = "Invalid refresh token")
    })
    public ResponseEntity<Object> refresh(
            @RequestBody java.util.Map<String,
                String> body) {

        String refreshToken = body.get("refreshToken");

        if (refreshToken == null ||
                refreshToken.isEmpty())
            return ResponseEntity.status(400)
                .body(com.example.demo.ApiResponse
                    .error("Refresh token is required!",
                        400));

        if (!jwtUtil.validateToken(refreshToken))
            return ResponseEntity.status(401)
                .body(com.example.demo.ApiResponse
                    .error("Invalid or expired " +
                        "refresh token!", 401));

        Long userId =
            jwtUtil.extractUserId(refreshToken);

        if (!refreshTokenService.validateRefreshToken(
                userId, refreshToken))
            return ResponseEntity.status(401)
                .body(com.example.demo.ApiResponse
                    .error("Refresh token mismatch! " +
                        "Please login again.", 401));

        java.util.Optional<User> optUser =
            userService.findById(userId);
        if (optUser.isEmpty())
            return ResponseEntity.status(404)
                .body(com.example.demo.ApiResponse
                    .error("User not found!", 404));

        User user = optUser.get();
        String newAccessToken =
            jwtUtil.generateAccessToken(
                user.getId(), user.getName(),
                user.getRole().toString());
        String newRefreshToken =
            jwtUtil.generateRefreshToken(user.getId());
        refreshTokenService.storeRefreshToken(
            user.getId(), newRefreshToken);

        return ResponseEntity.ok(
            com.example.demo.ApiResponse.success(
                java.util.Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken),
                "Token refreshed!"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user",
        description = "Invalidates the access token " +
            "and deletes the refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Logged out successfully"),
        @ApiResponse(responseCode = "401",
            description = "Invalid token")
    })
    public ResponseEntity<Object> logout(
            @RequestHeader("Authorization")
            String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(400)
                .body(com.example.demo.ApiResponse
                    .error("Authorization header " +
                        "missing!", 400));

        String accessToken = authHeader.substring(7);

        if (!jwtUtil.validateToken(accessToken))
            return ResponseEntity.status(401)
                .body(com.example.demo.ApiResponse
                    .error("Invalid token!", 401));

        Long userId =
            jwtUtil.extractUserId(accessToken);
        long expiry = jwtUtil
            .extractAllClaims(accessToken)
            .getExpiration().getTime()
            - System.currentTimeMillis();

        refreshTokenService.blacklistAccessToken(
            accessToken, expiry / 1000);
        refreshTokenService.deleteRefreshToken(userId);

        return ResponseEntity.ok(
            com.example.demo.ApiResponse.success(
                null, "Logged out successfully!"));
    }

    @GetMapping("/test-bcrypt")
    @Operation(summary = "Test BCrypt encoding",
        description = "Returns BCrypt hash of " +
            "'password123' for testing")
    public String testBcrypt() {
        BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();
        return encoder.encode("password123");
    }
}