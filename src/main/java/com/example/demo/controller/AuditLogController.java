package com.example.demo.controller;

import com.example.demo.model.AuditLog;
import com.example.demo.service.AuditLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    // ✅ GET /api/audit — paginated with filters
    @GetMapping
    public ResponseEntity<Object> getAll(
            @RequestParam(required = false)
                String actorId,
            @RequestParam(required = false)
                String action,
            @RequestParam(required = false)
                String entityType,
            @RequestParam(required = false)
                String from,
            @RequestParam(required = false)
                String to,
            @RequestParam(defaultValue = "0")
                int page,
            @RequestParam(defaultValue = "20")
                int size) {

        // If no filters, return paginated all
        if (actorId == null && action == null &&
            entityType == null &&
            from == null && to == null) {
            return ResponseEntity.ok(
                auditLogService.getLogsWithFilters(
                    null, null, null,
                    null, null, page, size));
        }

        return ResponseEntity.ok(
            auditLogService.getLogsWithFilters(
                actorId, action, entityType,
                from, to, page, size));
    }

    // ✅ GET /api/audit/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable UUID id) {
        Optional<AuditLog> result =
            auditLogService.getLogById(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }

    // ✅ POST /api/audit — manual log creation
    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody AuditLog log) {
        return ResponseEntity.status(201)
            .body(auditLogService.createLog(log));
    }

    // ✅ GET /api/audit/actor/{actorId}
    @GetMapping("/actor/{actorId}")
    public ResponseEntity<Object> getByActor(
            @PathVariable String actorId) {
        return ResponseEntity.ok(
            auditLogService.getLogsByActor(actorId));
    }

    // ✅ GET /api/audit/action/{action}
    @GetMapping("/action/{action}")
    public ResponseEntity<Object> getByAction(
            @PathVariable String action) {
        return ResponseEntity.ok(
            auditLogService.getLogsByAction(action));
    }

    // ✅ GET /api/audit/export — CSV download
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(required = false)
                String actorId,
            @RequestParam(required = false)
                String action,
            @RequestParam(required = false)
                String entityType,
            @RequestParam(required = false)
                String from,
            @RequestParam(required = false)
                String to,
            HttpServletResponse response)
            throws Exception {

        response.setContentType("text/csv");
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\"audit_log.csv\"");

        List<AuditLog> logs =
            auditLogService.getLogsForExport(
                actorId, action, entityType, from, to);

        PrintWriter writer = response.getWriter();

        // CSV Header
        writer.println(
            "Log ID,Actor ID,Action,Entity Type," +
            "Entity ID,Table Name,IP Address," +
            "Performed At,Old Value,New Value");

        // CSV Rows
        for (AuditLog log : logs) {
            writer.println(
                csvEscape(log.getLogId()) + "," +
                csvEscape(log.getActorId()) + "," +
                csvEscape(log.getAction()) + "," +
                csvEscape(log.getEntityType()) + "," +
                csvEscape(log.getEntityId()) + "," +
                csvEscape(log.getTableName()) + "," +
                csvEscape(log.getIpAddress()) + "," +
                csvEscape(log.getPerformedAt()) + "," +
                csvEscape(log.getOldValue()) + "," +
                csvEscape(log.getNewValue())
            );
        }
        writer.flush();
    }

    // ✅ Escape CSV values
    private String csvEscape(Object value) {
        if (value == null) return "";
        String str = value.toString()
            .replace("\"", "\"\"")
            .replace("\n", " ")
            .replace("\r", " ");
        if (str.contains(",") ||
            str.contains("\"") ||
            str.contains("\n")) {
            return "\"" + str + "\"";
        }
        return str;
    }
}