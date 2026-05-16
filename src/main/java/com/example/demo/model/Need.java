package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "needs")
public class Need {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "need_id")
    private UUID needId;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;
    private String type;

    @Column(name = "qty_required", nullable = false)
    private BigDecimal qtyRequired;

    @Column(name = "qty_fulfilled")
    private BigDecimal qtyFulfilled = BigDecimal.ZERO;

    private String status = "open";
    private String urgency = "medium";
    private LocalDate deadline;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}