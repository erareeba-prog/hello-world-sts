package com.example.demo.controller;

import com.example.demo.model.Donation;
import com.example.demo.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    @Autowired
    private DonationService donationService;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.ok(
            donationService.getAllDonations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable UUID id) {
        Optional<Donation> result =
            donationService.getDonationById(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }

    // ✅ GET /api/donations/{id}/receipt
    @GetMapping("/{id}/receipt")
    public ResponseEntity<Object> getReceipt(
            @PathVariable UUID id) {

        Object result = donationService.getReceipt(id);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Donation not found!");

        if (result.equals("RECEIPT_NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Receipt not found!");

        return ResponseEntity.ok(result);
    }

    // ✅ GET /api/donations/{id}/ngo-view
    // Returns donation with donor name hidden if anonymous
    @GetMapping("/{id}/ngo-view")
    public ResponseEntity<Object> getNgoView(
            @PathVariable UUID id) {

        Object result =
            donationService.getDonationForNgo(id);

        if (result.equals("NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Donation not found!");

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody Donation donation) {

        Object result =
            donationService.createDonation(donation);

        if (result.equals("INVALID_AMOUNT"))
            return ResponseEntity.status(400)
                .body("❌ Amount/Qty must be greater than 0!");

        if (result.equals("DONOR_ID_REQUIRED"))
            return ResponseEntity.status(400)
                .body("❌ Donor ID is required!");

        if (result.equals("NEED_ID_REQUIRED"))
            return ResponseEntity.status(400)
                .body("❌ Need ID is required!");

        if (result.equals("NEED_NOT_FOUND"))
            return ResponseEntity.status(404)
                .body("❌ Need not found!");

        if (result.equals("NEED_NOT_OPEN"))
            return ResponseEntity.status(400)
                .body("❌ Cannot donate — Need is not OPEN!");

        if (result.equals("DUPLICATE_DONATION"))
            return ResponseEntity.status(409)
                .body("⚠️ Duplicate donation detected!");

        return ResponseEntity.status(201).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable UUID id,
            @RequestBody Donation donation) {
        Object result =
            donationService.updateDonation(id, donation);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable UUID id,
            @RequestParam UUID deletedBy) {
        Object result =
            donationService.deleteDonation(id, deletedBy);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok("✅ Donation deleted!");
    }

    @GetMapping("/donor/{donorId}")
    public ResponseEntity<Object> getByDonor(
            @PathVariable UUID donorId) {
        return ResponseEntity.ok(
            donationService.getDonationsByDonor(donorId));
    }

    @GetMapping("/need/{needId}")
    public ResponseEntity<Object> getByNeed(
            @PathVariable UUID needId) {
        return ResponseEntity.ok(
            donationService.getDonationsByNeed(needId));
    }
}