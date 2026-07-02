package com.example.demo.controller;

import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // ✅ GET /api/notifications?userId=X&page=0&size=10
    @GetMapping
    public ResponseEntity<Object> getNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
            notificationService
                .getNotifications(userId, page, size));
    }

    // ✅ GET /api/notifications/last10?userId=X
    @GetMapping("/last10")
    public ResponseEntity<Object> getLast10(
            @RequestParam Long userId) {
        return ResponseEntity.ok(
            notificationService.getLast10(userId));
    }

    // ✅ GET /api/notifications/unread-count?userId=X
    @GetMapping("/unread-count")
    public ResponseEntity<Object> getUnreadCount(
            @RequestParam Long userId) {
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "unreadCount",
            notificationService.getUnreadCount(userId)
        ));
    }

    // ✅ PUT /api/notifications/{id}/read?userId=X
    @PutMapping("/{id}/read")
    public ResponseEntity<Object> markAsRead(
            @PathVariable UUID id,
            @RequestParam Long userId) {

        Object result =
            notificationService.markAsRead(id, userId);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Notification not found!");

        if (result.equals("UNAUTHORIZED"))
            return ResponseEntity.status(403)
                .body("❌ Not your notification!");

        return ResponseEntity.ok(result);
    }

    // ✅ PUT /api/notifications/mark-all-read?userId=X
    @PutMapping("/mark-all-read")
    public ResponseEntity<Object> markAllAsRead(
            @RequestParam Long userId) {

        int count =
            notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(Map.of(
            "message", "✅ Marked " + count +
                " notifications as read",
            "count", count
        ));
    }

    // ✅ DELETE /api/notifications/{id}?userId=X
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable UUID id,
            @RequestParam Long userId) {

        Object result =
            notificationService
                .deleteNotification(id, userId);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Notification not found!");

        if (result.equals("UNAUTHORIZED"))
            return ResponseEntity.status(403)
                .body("❌ Not your notification!");

        return ResponseEntity.ok(
            "✅ Notification deleted!");
    }
}	