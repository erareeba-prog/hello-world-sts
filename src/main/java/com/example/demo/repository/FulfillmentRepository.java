package com.example.demo.repository;

import com.example.demo.model.Fulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FulfillmentRepository
        extends JpaRepository<Fulfillment, UUID> {

    List<Fulfillment> findByNeedId(UUID needId);

    List<Fulfillment> findByDonationId(UUID donationId);

    boolean existsByDonationIdAndSelectedBy(
        UUID donationId, UUID selectedBy);

    // ✅ Count pending approvals (for admin)
    @Query("SELECT COUNT(f) FROM Fulfillment f " +
           "WHERE f.status = 'pending'")
    long countPendingApprovals();

    // ✅ Count confirmed fulfillments
    @Query("SELECT COUNT(f) FROM Fulfillment f " +
           "WHERE f.status = 'confirmed'")
    long countConfirmedFulfillments();

    // ✅ Count total fulfillments
    @Query("SELECT COUNT(f) FROM Fulfillment f")
    long countAllFulfillments();

    // ✅ Fulfillments for a specific NGO
    @Query("SELECT f FROM Fulfillment f " +
           "WHERE f.ngoId = :ngoId")
    List<Fulfillment> findByNgoId(
        @Param("ngoId") UUID ngoId);

    // ✅ Needs fulfilled through donor's donations
    @Query("SELECT f FROM Fulfillment f " +
           "WHERE f.donationId IN " +
           "(SELECT d.donationId FROM Donation d " +
           "WHERE d.donorId = :donorId " +
           "AND d.isDeleted = false) " +
           "AND f.status = 'confirmed'")
    List<Fulfillment> findConfirmedByDonorId(
        @Param("donorId") UUID donorId);
}