package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "providers")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "provider_id")
    private UUID providerId;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "org_type")
    private String orgType;

    private String gstn;
    private String address;
    private String website;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;
}