package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "donation_id", nullable = false)
    private UUID donationId;

    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    @Column(name = "need_id", nullable = false)
    private UUID needId;

    @Column(name = "ngo_id")
    private UUID ngoId;

    @Column(name = "donor_name")
    private String donorName;

    @Column(name = "ngo_name")
    private String ngoName;

    @Column(name = "need_title")
    private String needTitle;

    @Column(name = "amount_or_qty",
            nullable = false,
            precision = 15, scale = 2)
    private BigDecimal amountOrQty;

    private String currency = "INR";

    private String type;

    private String status = "pending";

    @Column(name = "receipt_id", unique = true)
    private String receiptId;

    // ✅ Anonymous flag
    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    // ✅ Tax deductible flag
    @Column(name = "tax_deductible")
    private Boolean taxDeductible = false;

    // ✅ On behalf of
    @Column(name = "on_behalf_of")
    private String onBehalfOf;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}