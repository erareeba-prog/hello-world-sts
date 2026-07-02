package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    public static final String TYPE_DONATION_CREATED    = "DONATION_CREATED";
    public static final String TYPE_NGO_APPROVED        = "NGO_APPROVED";
    public static final String TYPE_NGO_REJECTED        = "NGO_REJECTED";
    public static final String TYPE_FULFILLMENT_CONFIRMED = "FULFILLMENT_CONFIRMED";
    public static final String TYPE_OTP_SENT            = "OTP_SENT";
    public static final String TYPE_WELCOME             = "WELCOME";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private UUID notificationId;

    // User this notification belongs to (Long because User.id is Long)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    // Optional reference to related entity
    @Column(name = "ref_id")
    private String refId;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "read_at")
    private LocalDateTime readAt;
}