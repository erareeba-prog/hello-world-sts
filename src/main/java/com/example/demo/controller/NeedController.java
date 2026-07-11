package com.example.demo.controller;

import com.example.demo.model.Need;
import com.example.demo.service.NeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/needs")
public class NeedController {

    @Autowired
    private NeedService needService;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.ok(needService.getAllNeeds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable UUID id) {
        Optional<Need> result = needService.getNeedById(id);
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Need need) {
        return ResponseEntity.status(201)
            .body(needService.createNeed(need));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable UUID id, @RequestBody Need need) {
        Object result = needService.updateNeed(id, need);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable UUID id,
            @RequestParam UUID deletedBy) {
        Object result = needService.deleteNeed(id, deletedBy);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok("✅ Need deleted successfully!");
    }

    @GetMapping("/ngo/{ngoId}")
    public ResponseEntity<Object> getByNgo(@PathVariable UUID ngoId) {
        return ResponseEntity.ok(needService.getNeedsByNgo(ngoId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Object> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(
            needService.getNeedsByCategory(category));
    }

    @GetMapping("/urgency/{urgency}")
    public ResponseEntity<Object> getByUrgency(
            @PathVariable String urgency) {
        return ResponseEntity.ok(
            needService.getNeedsByUrgency(urgency));
    }
}