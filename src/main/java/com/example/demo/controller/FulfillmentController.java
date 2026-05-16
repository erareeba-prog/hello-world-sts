package com.example.demo.controller;

import com.example.demo.model.Fulfillment;
import com.example.demo.service.FulfillmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/fulfillments")
public class FulfillmentController {

    @Autowired
    private FulfillmentService fulfillmentService;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.ok(
            fulfillmentService.getAllFulfillments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable UUID id) {
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
            .body(fulfillmentService.createFulfillment(fulfillment));
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
    public ResponseEntity<Object> delete(@PathVariable UUID id) {
        Object result = fulfillmentService.deleteFulfillment(id);
        if (result.equals("NOT_FOUND"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok("✅ Fulfillment deleted!");
    }

    @GetMapping("/need/{needId}")
    public ResponseEntity<Object> getByNeed(
            @PathVariable UUID needId) {
        return ResponseEntity.ok(
            fulfillmentService.getFulfillmentsByNeed(needId));
    }
}