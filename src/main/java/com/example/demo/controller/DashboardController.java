package com.example.demo.controller;

import com.example.demo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // ✅ GET /api/dashboard/admin
    @GetMapping("/admin")
    public ResponseEntity<Object> getAdminDashboard() {
        return ResponseEntity.ok(
            dashboardService.getAdminDashboard());
    }

    // ✅ GET /api/dashboard/ngo/{id}
    @GetMapping("/ngo/{id}")
    public ResponseEntity<Object> getNgoDashboard(
            @PathVariable UUID id) {

        Object result =
            dashboardService.getNgoDashboard(id);

        if (result.equals("NGO_NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ NGO not found!");

        return ResponseEntity.ok(result);
    }

    // ✅ GET /api/dashboard/donor/{id}
    @GetMapping("/donor/{id}")
    public ResponseEntity<Object> getDonorDashboard(
            @PathVariable UUID id) {

        Object result =
            dashboardService.getDonorDashboard(id);

        return ResponseEntity.ok(result);
    }
}