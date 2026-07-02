package com.example.demo.controller;

import com.example.demo.model.Fulfillment;
import com.example.demo.service.FulfillmentService;
import com.example.demo.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/fulfillments")
public class FulfillmentController {

    @Autowired
    private FulfillmentService fulfillmentService;

    @Autowired
    private MatchingService matchingService;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.ok(
            fulfillmentService.getAllFulfillments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable UUID id) {
        Optional<Fulfillment> result =
            fulfillmentService.getFulfillmentById(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody Fulfillment fulfillment) {
        return ResponseEntity.status(201)
            .body(fulfillmentService
                .createFulfillment(fulfillment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable UUID id,
            @RequestBody Fulfillment fulfillment) {
        Object result = fulfillmentService
            .updateFulfillment(id, fulfillment);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable UUID id) {
        Object result = fulfillmentService
            .deleteFulfillment(id);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok("✅ Fulfillment deleted!");
    }

    @GetMapping("/need/{needId}")
    public ResponseEntity<Object> getByNeed(
            @PathVariable UUID needId) {
        return ResponseEntity.ok(
            fulfillmentService
                .getFulfillmentsByNeed(needId));
    }

    // ✅ PUT /api/fulfillments/{id}/confirm
    // NGO_STAFF only — confirms fulfillment,
    // updates need qty, triggers donation status update
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Object> confirm(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String staffIdStr = body.get("staffId");
        String providerIdStr = body.get("providerId");

        if (staffIdStr == null || staffIdStr.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ staffId is required!");

        if (providerIdStr == null ||
                providerIdStr.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ providerId is required!");

        UUID staffId;
        UUID providerId;
        try {
            staffId = UUID.fromString(staffIdStr);
            providerId = UUID.fromString(providerIdStr);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body("❌ Invalid UUID format!");
        }

        Object result = fulfillmentService
            .confirmFulfillment(id, staffId, providerId);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Fulfillment not found!");

        if (result.equals("NGO_ID_MISSING"))
            return ResponseEntity.status(400)
                .body("❌ Fulfillment has no NGO assigned!");

        if (result.equals("NOT_NGO_STAFF"))
            return ResponseEntity.status(403)
                .body("❌ Only NGO_STAFF can confirm fulfillments!");

        if (result.equals("ALREADY_CONFIRMED"))
            return ResponseEntity.status(409)
                .body("⚠️ Fulfillment already confirmed!");

        return ResponseEntity.ok(Map.of(
            "message",
                "✅ Fulfillment confirmed successfully!",
            "fulfillmentId", id.toString(),
            "status", "confirmed",
            "selectedBy", providerIdStr,
            "confirmedBy", staffIdStr
        ));
    }

    // ✅ POST /api/fulfillments/match/{donationId}
    // Match providers to a confirmed donation
    @PostMapping("/match/{donationId}")
    public ResponseEntity<Object> matchProviders(
            @PathVariable UUID donationId) {

        Object result =
            matchingService.matchProviders(donationId);

        if (result.equals("DONATION_NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Donation not found!");

        if (result.equals("DONATION_NOT_CONFIRMED"))
            return ResponseEntity.status(400)
                .body("❌ Donation must be CONFIRMED " +
                    "before matching!");

        if (result.equals("NEED_NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Need not found!");

        if (result.equals("NEED_CATEGORY_MISSING"))
            return ResponseEntity.status(400)
                .body("❌ Need has no category set!");

        if (result.equals("NO_PROVIDER_TYPE_FOR_CATEGORY"))
            return ResponseEntity.status(400)
                .body("❌ No provider type mapped for " +
                    "this category!");

        if (result.equals("NO_ELIGIBLE_PROVIDERS"))
            return ResponseEntity.status(404)
                .body("❌ No eligible providers found!");

        if (result.equals("FULFILLMENTS_ALREADY_EXIST"))
            return ResponseEntity.status(409)
                .body("⚠️ Fulfillments already created " +
                    "for this donation!");

        return ResponseEntity.status(201).body(result);
    }

    // ✅ GET /api/fulfillments/match/{donationId}/preview
    // Preview matching without creating fulfillments
    @GetMapping("/match/{donationId}/preview")
    public ResponseEntity<Object> previewMatch(
            @PathVariable UUID donationId) {

        Object result =
            matchingService.previewMatch(donationId);

        if (result instanceof String s) {
            return ResponseEntity.status(400).body(
                "❌ " + s);
        }

        return ResponseEntity.ok(result);
    }

    // ✅ PUT /api/fulfillments/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<Object> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String approverStr = body.get("approverId");
        if (approverStr == null || approverStr.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ approverId is required!");

        UUID approverId;
        try {
            approverId = UUID.fromString(approverStr);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body("❌ Invalid approverId UUID!");
        }

        Object result = fulfillmentService
            .approveFulfillment(id, approverId);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Fulfillment not found!");

        if (result.equals("NGO_ID_MISSING"))
            return ResponseEntity.status(400)
                .body("❌ Fulfillment has no NGO assigned!");

        if (result.equals("NOT_NGO_STAFF"))
            return ResponseEntity.status(403)
                .body("❌ Only NGO_STAFF can approve!");

        if (result.equals("ALREADY_APPROVED"))
            return ResponseEntity.status(409)
                .body("⚠️ Already approved!");

        return ResponseEntity.ok(Map.of(
            "message", "✅ Fulfillment approved!",
            "fulfillmentId", id.toString(),
            "status", "approved",
            "approvedBy", approverStr
        ));
    }

    // ✅ PUT /api/fulfillments/{id}/reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<Object> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String rejectorStr = body.get("rejectorId");
        String reason = body.get("reason");

        if (rejectorStr == null || rejectorStr.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ rejectorId is required!");

        if (reason == null || reason.trim().isEmpty())
            return ResponseEntity.status(400)
                .body("❌ Rejection reason is required!");

        UUID rejectorId;
        try {
            rejectorId = UUID.fromString(rejectorStr);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body("❌ Invalid rejectorId UUID!");
        }

        Object result = fulfillmentService
            .rejectFulfillment(id, rejectorId, reason);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Fulfillment not found!");

        if (result.equals("NGO_ID_MISSING"))
            return ResponseEntity.status(400)
                .body("❌ Fulfillment has no NGO assigned!");

        if (result.equals("NOT_NGO_STAFF"))
            return ResponseEntity.status(403)
                .body("❌ Only NGO_STAFF can reject!");

        if (result.equals("ALREADY_REJECTED"))
            return ResponseEntity.status(409)
                .body("⚠️ Already rejected!");

        return ResponseEntity.ok(Map.of(
            "message", "✅ Fulfillment rejected!",
            "fulfillmentId", id.toString(),
            "status", "rejected",
            "reason", reason,
            "rejectedBy", rejectorStr
        ));
    }
}