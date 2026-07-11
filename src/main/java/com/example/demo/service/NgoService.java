package com.example.demo.service;

import com.example.demo.model.Ngo;
import com.example.demo.repository.NgoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NgoService {

    @Autowired
    private NgoRepository ngoRepository;

    public List<Ngo> getAllNgos() {
        return ngoRepository.findByIsDeletedFalse();
    }

    public Optional<Ngo> getNgoById(UUID id) {
        return ngoRepository.findById(id);
    }

    public Object createNgo(Ngo ngo) {
        if (ngoRepository.existsByRegistrationNo(
                ngo.getRegistrationNo()))
            return "REG_NO_EXISTS";
        ngo.setCreatedAt(LocalDateTime.now());
        ngo.setVerificationStatus("pending");
        ngo.setIsDeleted(false);
        return ngoRepository.save(ngo);
    }

    public Object updateNgo(UUID id, Ngo updated) {
        Optional<Ngo> opt = ngoRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";
        Ngo ngo = opt.get();

        // ✅ Block updates if NGO is pending
        if ("pending".equals(ngo.getVerificationStatus()))
            return "NGO_PENDING";

        if (updated.getName() != null)
            ngo.setName(updated.getName());
        if (updated.getDescription() != null)
            ngo.setDescription(updated.getDescription());
        if (updated.getAddress() != null)
            ngo.setAddress(updated.getAddress());
        if (updated.getCity() != null)
            ngo.setCity(updated.getCity());
        if (updated.getState() != null)
            ngo.setState(updated.getState());
        if (updated.getContactPhone() != null)
            ngo.setContactPhone(updated.getContactPhone());
        if (updated.getWebsite() != null)
            ngo.setWebsite(updated.getWebsite());
        ngo.setUpdatedAt(LocalDateTime.now());
        return ngoRepository.save(ngo);
    }

    public Object deleteNgo(UUID id, UUID deletedBy) {
        Optional<Ngo> opt = ngoRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";
        Ngo ngo = opt.get();
        ngo.setIsDeleted(true);
        ngo.setDeletedBy(deletedBy);
        ngo.setDeletedAt(LocalDateTime.now());
        return ngoRepository.save(ngo);
    }

    // ✅ APPROVE NGO — Admin only
    public Object approveNgo(UUID id, UUID approvedBy) {
        Optional<Ngo> opt = ngoRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";

        Ngo ngo = opt.get();

        if ("active".equals(ngo.getVerificationStatus()))
            return "ALREADY_APPROVED";

        if ("rejected".equals(ngo.getVerificationStatus()))
            return "ALREADY_REJECTED";

        ngo.setVerificationStatus("active");
        ngo.setVerifiedBy(approvedBy);
        ngo.setVerifiedAt(LocalDateTime.now());
        ngo.setVerificationReason(null);
        ngo.setUpdatedAt(LocalDateTime.now());

        return ngoRepository.save(ngo);
    }

    // ✅ REJECT NGO — Admin only
    public Object rejectNgo(UUID id, UUID rejectedBy, String reason) {
        Optional<Ngo> opt = ngoRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";

        Ngo ngo = opt.get();

        if ("active".equals(ngo.getVerificationStatus()))
            return "ALREADY_APPROVED";

        if ("rejected".equals(ngo.getVerificationStatus()))
            return "ALREADY_REJECTED";

        if (reason == null || reason.trim().isEmpty())
            return "REASON_REQUIRED";

        ngo.setVerificationStatus("rejected");
        ngo.setVerifiedBy(rejectedBy);
        ngo.setVerifiedAt(LocalDateTime.now());
        ngo.setVerificationReason(reason);
        ngo.setUpdatedAt(LocalDateTime.now());

        return ngoRepository.save(ngo);
    }

    // ✅ Validate NGO is active (not pending/rejected)
    public boolean isNgoActive(UUID id) {
        Optional<Ngo> opt = ngoRepository.findById(id);
        if (opt.isEmpty()) return false;
        return "active".equals(opt.get().getVerificationStatus());
    }

    public List<Ngo> getNgosByStatus(String status) {
        return ngoRepository
            .findByVerificationStatusAndIsDeletedFalse(status);
    }

    public List<Ngo> getNgosByCity(String city) {
        return ngoRepository.findByCityAndIsDeletedFalse(city);
    }
}