package com.example.demo.controller;

import com.example.demo.model.NgoMember;
import com.example.demo.service.NgoMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ngo-members")
public class NgoMemberController {

    @Autowired
    private NgoMemberService ngoMemberService;

    // ✅ POST /api/ngo-members — Add member
    @PostMapping
    public ResponseEntity<Object> addMember(
            @RequestBody NgoMember member) {

        Object result = ngoMemberService.addMember(member);

        if (result.equals("NGO_ID_REQUIRED"))
            return ResponseEntity.status(400)
                .body("❌ NGO ID is required!");

        if (result.equals("USER_ID_REQUIRED"))
            return ResponseEntity.status(400)
                .body("❌ User ID is required!");

        if (result.equals("INVALID_ROLE"))
            return ResponseEntity.status(400)
                .body("❌ Invalid role! Must be NGO_STAFF or NGO_VOLUNTEER");

        if (result.equals("ALREADY_MEMBER"))
            return ResponseEntity.status(409)
                .body("⚠️ User is already a member of this NGO!");

        return ResponseEntity.status(201).body(result);
    }

    // ✅ GET /api/ngo-members/ngo/{ngoId} — All members of NGO
    @GetMapping("/ngo/{ngoId}")
    public ResponseEntity<Object> getMembersByNgo(
            @PathVariable UUID ngoId) {
        return ResponseEntity.ok(
            ngoMemberService.getMembersByNgo(ngoId));
    }

    // ✅ GET /api/ngo-members/user/{userId} — All NGOs of user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getNgosByUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(
            ngoMemberService.getNgosByUser(userId));
    }

    // ✅ GET /api/ngo-members/ngo/{ngoId}/role/{role}
    @GetMapping("/ngo/{ngoId}/role/{role}")
    public ResponseEntity<Object> getMembersByRole(
            @PathVariable UUID ngoId,
            @PathVariable String role) {

        if (!role.equals(NgoMember.ROLE_NGO_STAFF) &&
            !role.equals(NgoMember.ROLE_NGO_VOLUNTEER))
            return ResponseEntity.status(400)
                .body("❌ Invalid role! Use NGO_STAFF or NGO_VOLUNTEER");

        return ResponseEntity.ok(
            ngoMemberService.getMembersByRole(ngoId, role));
    }

    // ✅ PUT /api/ngo-members/{memberId}/role — Change role
    @PutMapping("/{memberId}/role")
    public ResponseEntity<Object> changeRole(
            @PathVariable UUID memberId,
            @RequestBody Map<String, String> body) {

        String newRole = body.get("role");
        String updatedByStr = body.get("updatedBy");

        if (newRole == null || newRole.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ New role is required!");

        if (updatedByStr == null || updatedByStr.isEmpty())
            return ResponseEntity.status(400)
                .body("❌ updatedBy is required!");

        UUID updatedBy;
        try {
            updatedBy = UUID.fromString(updatedByStr);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body("❌ Invalid updatedBy UUID!");
        }

        Object result = ngoMemberService.changeRole(
            memberId, newRole, updatedBy);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Member not found!");

        if (result.equals("INVALID_ROLE"))
            return ResponseEntity.status(400)
                .body("❌ Invalid role! Use NGO_STAFF or NGO_VOLUNTEER");

        return ResponseEntity.ok(Map.of(
            "message", "✅ Role updated successfully!",
            "memberId", memberId.toString(),
            "newRole", newRole
        ));
    }

    // ✅ DELETE /api/ngo-members/{memberId} — Remove member
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Object> removeMember(
            @PathVariable UUID memberId,
            @RequestParam UUID deletedBy) {

        Object result = ngoMemberService
            .removeMember(memberId, deletedBy);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Member not found!");

        return ResponseEntity.ok(
            "✅ Member removed successfully!");
    }
}