package com.example.demo.service;

import com.example.demo.model.Need;
import com.example.demo.repository.NeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NeedService {

    @Autowired
    private NeedRepository needRepository;

    public List<Need> getAllNeeds() {
        return needRepository.findByIsDeletedFalse();
    }

    public Optional<Need> getNeedById(UUID id) {
        return needRepository.findById(id);
    }

    public Need createNeed(Need need) {
        need.setCreatedAt(LocalDateTime.now());
        need.setIsDeleted(false);
        return needRepository.save(need);
    }

    public Object updateNeed(UUID id, Need updated) {
        Optional<Need> opt = needRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";
        Need need = opt.get();
        if (updated.getTitle() != null) need.setTitle(updated.getTitle());
        if (updated.getDescription() != null) need.setDescription(updated.getDescription());
        if (updated.getCategory() != null) need.setCategory(updated.getCategory());
        if (updated.getStatus() != null) need.setStatus(updated.getStatus());
        if (updated.getUrgency() != null) need.setUrgency(updated.getUrgency());
        if (updated.getQtyRequired() != null) need.setQtyRequired(updated.getQtyRequired());
        if (updated.getDeadline() != null) need.setDeadline(updated.getDeadline());
        need.setUpdatedAt(LocalDateTime.now());
        return needRepository.save(need);
    }

    public Object deleteNeed(UUID id, UUID deletedBy) {
        Optional<Need> opt = needRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";
        Need need = opt.get();
        need.setIsDeleted(true);
        need.setDeletedBy(deletedBy);
        need.setDeletedAt(LocalDateTime.now());
        return needRepository.save(need);
    }

    public List<Need> getNeedsByNgo(UUID ngoId) {
        return needRepository.findByNgoIdAndIsDeletedFalse(ngoId);
    }

    public List<Need> getNeedsByCategory(String category) {
        return needRepository.findByCategoryAndIsDeletedFalse(category);
    }

    public List<Need> getNeedsByUrgency(String urgency) {
        return needRepository.findByUrgencyAndIsDeletedFalse(urgency);
    }
}