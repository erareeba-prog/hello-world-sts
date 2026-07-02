package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "audit_log")
public class AuditLog {

    // ✅ Action constants
    public static final String ACTION_CREATE   = "CREATE";
    public static final String ACTION_UPDATE   = "UPDATE";
    public static final String ACTION_DELETE   = "DELETE";
    public static final String ACTION_APPROVE  = "APPROVE";
    public static final String ACTION_REJECT   = "REJECT";
    public static final String ACTION_LOGIN    = "LOGIN";
    public static final String ACTION_LOGOUT   = "LOGOUT";
    public static final String ACTION_REGISTER = "REGISTER";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private UUID logId;

    @Column(name = "actor_id")
    private String actorId; // String to support both Long and UUID

    @Column(name = "entity_type")
    private String entityType; // e.g. "Donation", "Ngo", "User"

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "record_id")
    private UUID recordId;

    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // JSON snapshot before

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // JSON snapshot after

    @Column(name = "changed_fields")
    private String changedFields;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "performed_at")
    private LocalDateTime performedAt =
        LocalDateTime.now();
}