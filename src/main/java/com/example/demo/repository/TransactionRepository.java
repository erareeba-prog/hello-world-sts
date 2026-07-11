package com.example.demo.repository;

import com.example.demo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, String> {

    // Find by donation ID
    Optional<Transaction> findByDonationId(UUID donationId);

    // Find by receipt ID
    Optional<Transaction> findByReceiptId(String receiptId);

    // Find all transactions by donor
    List<Transaction> findByDonorId(UUID donorId);

    // Find all transactions by NGO
    List<Transaction> findByNgoId(UUID ngoId);

    boolean existsByDonationId(UUID donationId);
}