package com.example.demo.service;

import com.example.demo.model.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public Optional<AuditLog> getLogById(UUID id) {
        return auditLogRepository.findById(id);
    }

    public AuditLog createLog(AuditLog log) {
        log.setPerformedAt(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByActor(UUID actorId) {
        return auditLogRepository.findByActorId(actorId);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }
}