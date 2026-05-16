package com.example.demo.controller;

import com.example.demo.model.Donation;
import com.example.demo.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    @Autowired
    private DonationService donationService;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.ok(donationService.getAllDonations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable UUID id) {
        Optional<Donation> result = donationService.getDonationById(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }
    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody Donation donation) {
        return ResponseEntity.status(201)
            .body(donationService.createDonation(donation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable UUID id,
            @RequestBody Donation donation) {
        Object result = donationService.updateDonation(id, donation);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable UUID id,
            @RequestParam UUID deletedBy) {
        Object result = donationService.deleteDonation(id, deletedBy);
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