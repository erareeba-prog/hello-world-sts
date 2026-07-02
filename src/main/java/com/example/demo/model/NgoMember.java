package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "ngo_members")
public class NgoMember {

    // ✅ Role constants
    public static final String ROLE_NGO_STAFF     = "NGO_STAFF";
    public static final String ROLE_NGO_VOLUNTEER = "NGO_VOLUNTEER";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String role; // NGO_STAFF or NGO_VOLUNTEER

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "invited_by")
    private UUID invitedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;
}