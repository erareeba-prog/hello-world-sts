package com.example.demo.controller;

import com.example.demo.model.Ngo;
import com.example.demo.service.NgoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Optional;

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
    public ResponseEntity<Object> getById(@PathVariable UUID id) {
        Optional<Ngo> result = ngoService.getNgoById(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Ngo ngo) {
        Object result = ngoService.createNgo(ngo);
        if (result.equals("REG_NO_EXISTS"))
            return ResponseEntity.status(409)
                .body("❌ Registration number already exists!");
        return ResponseEntity.status(201).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable UUID id, @RequestBody Ngo ngo) {
        Object result = ngoService.updateNgo(id, ngo);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
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
        return ResponseEntity.ok(ngoService.getNgosByStatus(status));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<Object> getByCity(
            @PathVariable String city) {
        return ResponseEntity.ok(ngoService.getNgosByCity(city));
    }
}