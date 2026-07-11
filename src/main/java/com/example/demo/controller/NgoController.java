package com.example.demo.controller;

import com.example.demo.model.Ngo;
import com.example.demo.service.NgoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/ngos")
public class NgoController {

    @Autowired
    private NgoService ngoService;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.ok(ngoService.getAllNgos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable UUID id) {
        Optional<Ngo> result = ngoService.getNgoById(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody Ngo ngo) {
        Object result = ngoService.createNgo(ngo);
        if (result.equals("REG_NO_EXISTS"))
            return ResponseEntity.status(409)
                .body("❌ Registration number already exists!");
        return ResponseEntity.status(201).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable UUID id,
            @RequestBody Ngo ngo) {
        Object result = ngoService.updateNgo(id, ngo);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        // ✅ Block if pending
        if (result.equals("NGO_PENDING"))
            return ResponseEntity.status(403)
                .body("❌ NGO cannot be updated while status is PENDING!");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable UUID id,
            @RequestParam UUID deletedBy) {
        Object result = ngoService.deleteNgo(id, deletedBy);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok("✅ NGO deleted successfully!");
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Object> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(
            ngoService.getNgosByStatus(status));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<Object> getByCity(
            @PathVariable String city) {
        return ResponseEntity.ok(
            ngoService.getNgosByCity(city));
    }

    // ✅ PUT /api/ngos/{id}/approve — Admin only
    @PutMapping("/{id}/approve")
    public ResponseEntity<Object> approveNgo(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String approvedByStr = body.get("approvedBy");
        if (approvedByStr == null || approvedByStr.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ approvedBy is required!");

        UUID approvedBy;
        try {
            approvedBy = UUID.fromString(approvedByStr);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body("❌ Invalid approvedBy UUID format!");
        }

        Object result = ngoService.approveNgo(id, approvedBy);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ NGO not found!");

        if (result.equals("ALREADY_APPROVED"))
            return ResponseEntity.status(409)
                .body("⚠️ NGO is already approved!");

        if (result.equals("ALREADY_REJECTED"))
            return ResponseEntity.status(409)
                .body("⚠️ NGO is already rejected! Cannot approve.");

        return ResponseEntity.ok(Map.of(
            "message", "✅ NGO approved successfully!",
            "ngoId", id.toString(),
            "status", "active",
            "approvedBy", approvedByStr
        ));
    }

    // ✅ PUT /api/ngos/{id}/reject — Admin only
    @PutMapping("/{id}/reject")
    public ResponseEntity<Object> rejectNgo(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String rejectedByStr = body.get("rejectedBy");
        String reason = body.get("reason");

        if (rejectedByStr == null || rejectedByStr.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ rejectedBy is required!");

        if (reason == null || reason.trim().isEmpty())
            return ResponseEntity.status(400)
                .body("❌ Rejection reason is required!");

        UUID rejectedBy;
        try {
            rejectedBy = UUID.fromString(rejectedByStr);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body("❌ Invalid rejectedBy UUID format!");
        }

        Object result = ngoService.rejectNgo(id, rejectedBy, reason);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ NGO not found!");

        if (result.equals("ALREADY_APPROVED"))
            return ResponseEntity.status(409)
                .body("⚠️ NGO is already approved! Cannot reject.");

        if (result.equals("ALREADY_REJECTED"))
            return ResponseEntity.status(409)
                .body("⚠️ NGO is already rejected!");

        if (result.equals("REASON_REQUIRED"))
            return ResponseEntity.status(400)
                .body("❌ Rejection reason is required!");

        return ResponseEntity.ok(Map.of(
            "message", "✅ NGO rejected successfully!",
            "ngoId", id.toString(),
            "status", "rejected",
            "reason", reason,
            "rejectedBy", rejectedByStr
        ));
    }
}