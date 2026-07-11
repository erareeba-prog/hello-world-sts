package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    @Autowired
    private UserService userService;

    // GET /api/users/me — get my profile
    @GetMapping("/me")
    public ResponseEntity<Object> getMyProfile(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        Optional<User> user = userService.findById(userId);

        if (user.isEmpty())
            return ResponseEntity.status(404)
                .body("❌ User not found!");

        return ResponseEntity.ok(user.get());
    }

    // PUT /api/users/me — update my profile
    @PutMapping("/me")
    public ResponseEntity<Object> updateMyProfile(
            Authentication authentication,
            @RequestBody AuthRequest request) {

        Long userId = (Long) authentication.getPrincipal();

        Object result = userService.updateProfile(userId, request);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ User not found!");

        if (result.equals("EMAIL_EXISTS"))
            return ResponseEntity.status(409)
                .body("❌ Email already taken!");

        return ResponseEntity.ok(result);
    }

    // GET /api/users — only ADMIN can see all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/users/role/{role} — ADMIN only
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getUsersByRole(
            @PathVariable String role) {
        return ResponseEntity.ok(
            userService.getUsersByRole(
                Role.valueOf(role.toUpperCase())));
    }
}