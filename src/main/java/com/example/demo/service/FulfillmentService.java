package com.example.demo.service;

import com.example.demo.model.Fulfillment;
import com.example.demo.repository.FulfillmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FulfillmentService {

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    public List<Fulfillment> getAllFulfillments() {
        return fulfillmentRepository.findAll();
    }

    public Optional<Fulfillment> getFulfillmentById(UUID id) {
        return fulfillmentRepository.findById(id);
    }

    public Fulfillment createFulfillment(Fulfillment fulfillment) {
        fulfillment.setFulfilledAt(LocalDateTime.now());
        return fulfillmentRepository.save(fulfillment);
    }

    public Object updateFulfillment(UUID id, Fulfillment updated) {
        Optional<Fulfillment> opt = fulfillmentRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";
        Fulfillment f = opt.get();
        if (updated.getQtyApplied() != null) f.setQtyApplied(updated.getQtyApplied());
        if (updated.getProofUrl() != null) f.setProofUrl(updated.getProofUrl());
        if (updated.getNotes() != null) f.setNotes(updated.getNotes());
        f.setUpdatedAt(LocalDateTime.now());
        return fulfillmentRepository.save(f);
    }

    public Object deleteFulfillment(UUID id) {
        if (!fulfillmentRepository.existsById(id)) return "NOT_FOUND";
        fulfillmentRepository.deleteById(id);
        return "DELETED";
    }

    public List<Fulfillment> getFulfillmentsByNeed(UUID needId) {
        return fulfillmentRepository.findByNeedId(needId);
    }
}