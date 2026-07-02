package com.example.demo.service;

import com.example.demo.model.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ Core method — log any action
    public AuditLog logAction(
            String actorId,
            String action,
            String entityType,
            String entityId,
            Object oldValue,
            Object newValue,
            String ipAddress) {

        AuditLog log = new AuditLog();
        log.setActorId(actorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setTableName(
            entityType != null
                ? entityType.toLowerCase() + "s"
                : "unknown");
        log.setIpAddress(ipAddress);
        log.setPerformedAt(LocalDateTime.now());

        // ✅ Serialize old/new values to JSON
        try {
            if (oldValue != null)
                log.setOldValue(
                    objectMapper.writeValueAsString(
                        oldValue));
            if (newValue != null)
                log.setNewValue(
                    objectMapper.writeValueAsString(
                        newValue));
        } catch (Exception e) {
            log.setOldValue(
                oldValue != null
                    ? oldValue.toString() : null);
            log.setNewValue(
                newValue != null
                    ? newValue.toString() : null);
        }

        AuditLog saved = auditLogRepository.save(log);
        System.out.println(
            "📋 AUDIT: [" + action + "] " +
            entityType + "#" + entityId +
            " by actor " + actorId);
        return saved;
    }

    // ✅ Overload — without IP
    public AuditLog logAction(
            String actorId,
            String action,
            String entityType,
            String entityId,
            Object oldValue,
            Object newValue) {
        return logAction(actorId, action, entityType,
            entityId, oldValue, newValue, null);
    }

    // ✅ GET all logs
    public List<AuditLog> getAllLogs() {
        return auditLogRepository
            .findAll(Sort.by(
                Sort.Direction.DESC, "performedAt"));
    }

    // ✅ GET by ID
    public Optional<AuditLog> getLogById(UUID id) {
        return auditLogRepository.findById(id);
    }

    // ✅ CREATE log manually
    public AuditLog createLog(AuditLog log) {
        log.setPerformedAt(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    // ✅ GET by actor
    public List<AuditLog> getLogsByActor(
            String actorId) {
        return auditLogRepository
            .findByActorId(actorId);
    }

    // ✅ GET by action
    public List<AuditLog> getLogsByAction(
            String action) {
        return auditLogRepository
            .findByAction(action);
    }

    // ✅ GET with filters (paginated)
    public Page<AuditLog> getLogsWithFilters(
            String actorId,
            String action,
            String entityType,
            String from,
            String to,
            int page,
            int size) {

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        try {
            if (from != null && !from.isEmpty())
                fromDate = LocalDateTime.parse(from,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                fromDate = LocalDateTime.parse(
                    from + "T00:00:00");
            } catch (Exception ignored) {}
        }

        try {
            if (to != null && !to.isEmpty())
                toDate = LocalDateTime.parse(to,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                toDate = LocalDateTime.parse(
                    to + "T23:59:59");
            } catch (Exception ignored) {}
        }

        return auditLogRepository.findWithFilters(
            actorId,
            action,
            entityType,
            fromDate,
            toDate,
            PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC,
                    "performedAt"))
        );
    }

    // ✅ GET all for CSV export
    public List<AuditLog> getLogsForExport(
            String actorId,
            String action,
            String entityType,
            String from,
            String to) {

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        try {
            if (from != null && !from.isEmpty())
                fromDate = LocalDateTime.parse(
                    from + "T00:00:00");
        } catch (Exception ignored) {}

        try {
            if (to != null && !to.isEmpty())
                toDate = LocalDateTime.parse(
                    to + "T23:59:59");
        } catch (Exception ignored) {}

        return auditLogRepository.findAllWithFilters(
            actorId, action, entityType,
            fromDate, toDate);
    }
}