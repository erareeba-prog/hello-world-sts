package com.example.demo.repository;

import com.example.demo.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRepository
        extends JpaRepository<Donation, UUID> {

    List<Donation> findByIsDeletedFalse();

    List<Donation> findByDonorIdAndIsDeletedFalse(
        UUID donorId);

    List<Donation> findByNeedIdAndIsDeletedFalse(
        UUID needId);

    Optional<Donation> findByIdempotencyKey(
        String idempotencyKey);

    boolean existsByIdempotencyKey(
        String idempotencyKey);

    // ✅ Total donation amount (all confirmed)
    @Query("SELECT COALESCE(SUM(d.amountOrQty), 0) " +
           "FROM Donation d " +
           "WHERE d.isDeleted = false " +
           "AND d.status = 'confirmed'")
    BigDecimal getTotalDonationAmount();

    // ✅ Total donations for a specific donor
    @Query("SELECT COALESCE(SUM(d.amountOrQty), 0) " +
           "FROM Donation d " +
           "WHERE d.donorId = :donorId " +
           "AND d.isDeleted = false")
    BigDecimal getTotalAmountByDonor(
        @Param("donorId") UUID donorId);

    // ✅ Count donations by status
    @Query("SELECT COUNT(d) FROM Donation d " +
           "WHERE d.isDeleted = false " +
           "AND d.status = :status")
    long countByStatus(@Param("status") String status);

    // ✅ Donations for needs belonging to an NGO
    @Query("SELECT d FROM Donation d " +
           "WHERE d.needId IN " +
           "(SELECT n.needId FROM Need n " +
           "WHERE n.ngoId = :ngoId " +
           "AND n.isDeleted = false) " +
           "AND d.isDeleted = false")
    List<Donation> findByNgoId(
        @Param("ngoId") UUID ngoId);

    // ✅ Total funds received by NGO
    @Query("SELECT COALESCE(SUM(d.amountOrQty), 0) " +
           "FROM Donation d " +
           "WHERE d.needId IN " +
           "(SELECT n.needId FROM Need n " +
           "WHERE n.ngoId = :ngoId " +
           "AND n.isDeleted = false) " +
           "AND d.isDeleted = false " +
           "AND d.status = 'confirmed'")
    BigDecimal getTotalFundsReceivedByNgo(
        @Param("ngoId") UUID ngoId);

    // ✅ Count distinct NGOs supported by donor
    @Query("SELECT COUNT(DISTINCT n.ngoId) " +
           "FROM Donation d " +
           "JOIN Need n ON d.needId = n.needId " +
           "WHERE d.donorId = :donorId " +
           "AND d.isDeleted = false")
    long countNgosSupportedByDonor(
        @Param("donorId") UUID donorId);

    // ✅ Top donors for an NGO (anonymous-aware)
    @Query("SELECT d.donorId, " +
           "SUM(d.amountOrQty) as total, " +
           "MAX(CASE WHEN d.isAnonymous = true " +
           "THEN true ELSE false END) as anon " +
           "FROM Donation d " +
           "WHERE d.needId IN " +
           "(SELECT n.needId FROM Need n " +
           "WHERE n.ngoId = :ngoId " +
           "AND n.isDeleted = false) " +
           "AND d.isDeleted = false " +
           "GROUP BY d.donorId " +
           "ORDER BY total DESC")
    List<Object[]> getTopDonorsByNgo(
        @Param("ngoId") UUID ngoId);
}