package com.example.demo.repository;

import com.example.demo.model.Fulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, UUID> {
    List<Fulfillment> findByNeedId(UUID needId);
    List<Fulfillment> findByDonationId(UUID donationId);
    List<Fulfillment> findByRecordedBy(UUID recordedBy);
}