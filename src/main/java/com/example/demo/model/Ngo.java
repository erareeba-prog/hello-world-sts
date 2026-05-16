package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "ngos")
public class Ngo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ngo_id")
    private UUID ngoId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(nullable = false)
    private String name;

    @Column(name = "registration_no", unique = true, nullable = false)
    private String registrationNo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "contact_phone")
    private String contactPhone;

    private String website;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    private String country = "India";

    @Column(name = "verification_status")
    private String verificationStatus = "pending";

    @Column(name = "verification_reason", columnDefinition = "TEXT")
    private String verificationReason;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}