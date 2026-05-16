package com.example.demo.controller;

import com.example.demo.model.AuditLog;
import com.example.demo.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable UUID id) {
        Optional<AuditLog> result = auditLogService.getLogById(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody AuditLog log) {
        return ResponseEntity.status(201)
            .body(auditLogService.createLog(log));
    }

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<Object> getByActor(
            @PathVariable UUID actorId) {
        return ResponseEntity.ok(
            auditLogService.getLogsByActor(actorId));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<Object> getByAction(
            @PathVariable String action) {
        return ResponseEntity.ok(
            auditLogService.getLogsByAction(action));
    }
}