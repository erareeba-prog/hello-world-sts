package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // ✅ Use @Autowired with required=false to avoid
    // startup failure if WebSocket not configured yet
    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    // ✅ Save notification to DB and push via WebSocket
    public Notification createNotification(
            Long userId,
            String type,
            String title,
            String message,
            String refId) {

        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(type);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setRefId(refId);
        notif.setIsRead(false);
        notif.setCreatedAt(LocalDateTime.now());

        Notification saved =
            notificationRepository.save(notif);

        // Push via WebSocket
        pushToUser(userId, saved);

        return saved;
    }

    // ✅ Push via WebSocket — null check added
    private void pushToUser(Long userId,
                             Notification notif) {
        if (messagingTemplate == null) {
            System.out.println(
                "⚠️ WebSocket not available, " +
                "skipping push for user " + userId);
            return;
        }
        try {
            long unreadCount =
                notificationRepository
                    .countByUserIdAndIsReadFalse(userId);

            Map<String, Object> payload =
                new HashMap<>();
            payload.put("notificationId",
                notif.getNotificationId().toString());
            payload.put("type",    notif.getType());
            payload.put("title",   notif.getTitle());
            payload.put("message", notif.getMessage());
            payload.put("unreadCount", unreadCount);
            payload.put("createdAt",
                notif.getCreatedAt().toString());

            // ✅ Cast destination explicitly as String
            String destination =
            		"/topic/notifications/" + userId;
            messagingTemplate.convertAndSend(
            		destination, (Object) payload);

            System.out.println(
                "🔔 WebSocket push → user " + userId +
                " | " + notif.getTitle());

        } catch (Exception e) {
            System.err.println(
                "❌ WebSocket push failed: " +
                e.getMessage());
        }
    }

    // ✅ GET notifications (paginated)
    public Page<Notification> getNotifications(
            Long userId, int page, int size) {
        return notificationRepository
            .findByUserIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(page, size));
    }

    // ✅ GET last 10 notifications
    public List<Notification> getLast10(Long userId) {
        return notificationRepository
            .findTop10ByUserIdOrderByCreatedAtDesc(
                userId);
    }

    // ✅ GET unread count
    public long getUnreadCount(Long userId) {
        return notificationRepository
            .countByUserIdAndIsReadFalse(userId);
    }

    // ✅ PUT mark single notification as read
    public Object markAsRead(UUID notificationId,
                              Long userId) {
        Optional<Notification> opt =
            notificationRepository
                .findById(notificationId);
        if (opt.isEmpty()) return "NOT_FOUND";

        Notification notif = opt.get();
        if (!notif.getUserId().equals(userId))
            return "UNAUTHORIZED";

        notif.setIsRead(true);
        notif.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notif);
    }

    // ✅ PUT mark all as read
    @Transactional
    public int markAllAsRead(Long userId) {
        int count =
            notificationRepository.markAllAsRead(userId);

        // Push updated count via WebSocket
        if (messagingTemplate != null) {
            try {
            	java.util.LinkedHashMap<String, Object>
            	payload =
            	new java.util.LinkedHashMap<>();
            	payload.put("unreadCount", 0);
            	payload.put("type", "MARK_ALL_READ");
            	String destination =
            			"/topic/notifications/" + userId;
            	messagingTemplate.convertAndSend(
            			destination, (Object) payload);
            } catch (Exception e) {
                System.err.println(
                    "❌ WebSocket push failed: " +
                    e.getMessage());
            }
        }

        return count;
    }

    // ✅ DELETE notification
    public Object deleteNotification(
            UUID notificationId,
            Long userId) {
        Optional<Notification> opt =
            notificationRepository
                .findById(notificationId);
        if (opt.isEmpty()) return "NOT_FOUND";

        Notification notif = opt.get();
        if (!notif.getUserId().equals(userId))
            return "UNAUTHORIZED";

        notificationRepository.delete(notif);
        return "DELETED";
    }
}