package com.example.demo.repository;

import com.example.demo.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {
    List<Donation> findByIsDeletedFalse();
    List<Donation> findByDonorIdAndIsDeletedFalse(UUID donorId);
    List<Donation> findByNeedIdAndIsDeletedFalse(UUID needId);
    List<Donation> findByStatusAndIsDeletedFalse(String status);
}