package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "donations")
public class Donation {

    public static final String STATUS_PENDING   = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_CANCELLED = "cancelled";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "donation_id")
    private UUID donationId;

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "need_id", nullable = false)
    private UUID needId;

    @Column(name = "provider_id")
    private UUID providerId;

    private String type;

    @Column(name = "amount_or_qty", nullable = false)
    private BigDecimal amountOrQty;

    private String currency = "INR";

    private String status = STATUS_PENDING;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "receipt_id")
    private String receiptId;

    @Column(name = "transaction_ref")
    private String transactionRef;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    // ✅ Anonymous flag — hides donor name in NGO view
    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    // ✅ Tax deductible flag — included in receipt PDF
    @Column(name = "tax_deductible")
    private Boolean taxDeductible = false;

    // ✅ On behalf of text field
    @Column(name = "on_behalf_of")
    private String onBehalfOf;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "donated_at")
    private LocalDateTime donatedAt = LocalDateTime.now();

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;
}