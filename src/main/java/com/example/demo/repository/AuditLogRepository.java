package com.example.demo.repository;

import com.example.demo.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByActorId(String actorId);

    List<AuditLog> findByTableNameAndRecordId(
        String tableName, UUID recordId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByEntityType(String entityType);

    // ✅ Paginated - all logs
    Page<AuditLog> findAllByOrderByPerformedAtDesc(
        Pageable pageable);

    // ✅ Filter by date range
    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:actorId IS NULL OR " +
           "a.actorId = :actorId) " +
           "AND (:action IS NULL OR " +
           "a.action = :action) " +
           "AND (:entityType IS NULL OR " +
           "a.entityType = :entityType) " +
           "AND (:from IS NULL OR " +
           "a.performedAt >= :from) " +
           "AND (:to IS NULL OR " +
           "a.performedAt <= :to) " +
           "ORDER BY a.performedAt DESC")
    Page<AuditLog> findWithFilters(
        @Param("actorId")     String actorId,
        @Param("action")      String action,
        @Param("entityType")  String entityType,
        @Param("from")        LocalDateTime from,
        @Param("to")          LocalDateTime to,
        Pageable pageable);

    // ✅ For CSV export — no pagination
    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:actorId IS NULL OR " +
           "a.actorId = :actorId) " +
           "AND (:action IS NULL OR " +
           "a.action = :action) " +
           "AND (:entityType IS NULL OR " +
           "a.entityType = :entityType) " +
           "AND (:from IS NULL OR " +
           "a.performedAt >= :from) " +
           "AND (:to IS NULL OR " +
           "a.performedAt <= :to) " +
           "ORDER BY a.performedAt DESC")
    List<AuditLog> findAllWithFilters(
        @Param("actorId")     String actorId,
        @Param("action")      String action,
        @Param("entityType")  String entityType,
        @Param("from")        LocalDateTime from,
        @Param("to")          LocalDateTime to);
}