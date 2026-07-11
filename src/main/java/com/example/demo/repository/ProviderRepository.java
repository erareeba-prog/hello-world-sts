package com.example.demo.repository;

import com.example.demo.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    List<Provider> findByIsDeletedFalse();
    Optional<Provider> findByUserId(UUID userId);
    List<Provider> findByIsVerifiedAndIsDeletedFalse(Boolean isVerified);
}