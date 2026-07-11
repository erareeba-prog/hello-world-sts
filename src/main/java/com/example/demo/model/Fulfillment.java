package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "fulfillments")
public class Fulfillment {

    // ✅ Status constants
    public static final String STATUS_PENDING   = "pending";
    public static final String STATUS_APPROVED  = "approved";
    public static final String STATUS_REJECTED  = "rejected";
    public static final String STATUS_CONFIRMED = "confirmed";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "fulfillment_id")
    private UUID fulfillmentId;

    @Column(name = "need_id", nullable = false)
    private UUID needId;

    @Column(name = "donation_id", unique = true,
            nullable = false)
    private UUID donationId;

    @Column(name = "recorded_by", nullable = false)
    private UUID recordedBy;

    @Column(name = "qty_applied", nullable = false)
    private BigDecimal qtyApplied;

    @Column(name = "proof_url")
    private String proofUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status")
    private String status = STATUS_PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason",
            columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "ngo_id")
    private UUID ngoId;

    // ✅ Provider who was selected for this fulfillment
    @Column(name = "selected_by")
    private UUID selectedBy;

    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;
}