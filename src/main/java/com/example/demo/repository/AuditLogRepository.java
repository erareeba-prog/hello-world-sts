package com.example.demo.repository;

import com.example.demo.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByActorId(UUID actorId);
    List<AuditLog> findByTableNameAndRecordId(String tableName, UUID recordId);
    List<AuditLog> findByAction(String action);
}