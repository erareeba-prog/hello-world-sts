package com.example.demo.repository;

import com.example.demo.model.Ngo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NgoRepository
        extends JpaRepository<Ngo, UUID> {

    List<Ngo> findByIsDeletedFalse();

    List<Ngo> findByVerificationStatusAndIsDeletedFalse(
        String status);

    List<Ngo> findByCityAndIsDeletedFalse(String city);

    boolean existsByRegistrationNo(String registrationNo);

    // ✅ Count active NGOs
    @Query("SELECT COUNT(n) FROM Ngo n " +
           "WHERE n.verificationStatus = 'active' " +
           "AND n.isDeleted = false")
    long countActiveNgos();
}