package com.example.demo.repository;

import com.example.demo.model.Need;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NeedRepository
        extends JpaRepository<Need, UUID> {

    List<Need> findByIsDeletedFalse();

    List<Need> findByNgoIdAndIsDeletedFalse(UUID ngoId);

    List<Need> findByStatusAndIsDeletedFalse(String status);

    List<Need> findByCategoryAndIsDeletedFalse(
        String category);

    List<Need> findByUrgencyAndIsDeletedFalse(
        String urgency);

    // ✅ FIXED: removed boolean parameter
    // IsDeletedFalse is a condition, not a parameter
    boolean existsByNeedIdAndStatusAndIsDeletedFalse(
        UUID needId, String status);

    // Dashboard queries
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(n) FROM Need n " +
        "WHERE n.ngoId = :ngoId " +
        "AND n.status = 'open' " +
        "AND n.isDeleted = false")
    long countOpenNeedsByNgo(
        @org.springframework.data.repository.query
            .Param("ngoId") UUID ngoId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(n) FROM Need n " +
        "WHERE n.ngoId = :ngoId " +
        "AND n.status = 'closed' " +
        "AND n.isDeleted = false")
    long countFulfilledNeedsByNgo(
        @org.springframework.data.repository.query
            .Param("ngoId") UUID ngoId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(n) FROM Need n " +
        "WHERE n.isDeleted = false")
    long countAllNeeds();

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(n) FROM Need n " +
        "WHERE n.status = 'closed' " +
        "AND n.isDeleted = false")
    long countAllFulfilledNeeds();
}