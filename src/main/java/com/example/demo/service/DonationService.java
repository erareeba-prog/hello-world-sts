package com.example.demo.service;

import com.example.demo.model.Donation;
import com.example.demo.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    public List<Donation> getAllDonations() {
        return donationRepository.findByIsDeletedFalse();
    }

    public Optional<Donation> getDonationById(UUID id) {
        return donationRepository.findById(id);
    }

    public Donation createDonation(Donation donation) {
        donation.setDonatedAt(LocalDateTime.now());
        donation.setIsDeleted(false);
        donation.setStatus("pending");
        return donationRepository.save(donation);
    }

    public Object updateDonation(UUID id, Donation updated) {
        Optional<Donation> opt = donationRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";
        Donation donation = opt.get();
        if (updated.getStatus() != null) donation.setStatus(updated.getStatus());
        if (updated.getTransactionRef() != null) donation.setTransactionRef(updated.getTransactionRef());
        if (updated.getCancellationReason() != null) donation.setCancellationReason(updated.getCancellationReason());
        donation.setUpdatedAt(LocalDateTime.now());
        return donationRepository.save(donation);
    }

    public Object deleteDonation(UUID id, UUID deletedBy) {
        Optional<Donation> opt = donationRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";
        Donation donation = opt.get();
        donation.setIsDeleted(true);
        donation.setDeletedBy(deletedBy);
        donation.setDeletedAt(LocalDateTime.now());
        return donationRepository.save(donation);
    }

    public List<Donation> getDonationsByDonor(UUID donorId) {
        return donationRepository.findByDonorIdAndIsDeletedFalse(donorId);
    }

    public List<Donation> getDonationsByNeed(UUID needId) {
        return donationRepository.findByNeedIdAndIsDeletedFalse(needId);
    }
}