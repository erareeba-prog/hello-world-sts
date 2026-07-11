package com.example.demo.repository;

import com.example.demo.model.NgoMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NgoMemberRepository
        extends JpaRepository<NgoMember, UUID> {

    List<NgoMember> findByNgoIdAndIsDeletedFalse(UUID ngoId);

    List<NgoMember> findByUserIdAndIsDeletedFalse(UUID userId);

    boolean existsByNgoIdAndUserId(UUID ngoId, UUID userId);

    // ✅ Find by ngoId + userId (active)
    Optional<NgoMember> findByNgoIdAndUserIdAndIsDeletedFalse(
        UUID ngoId, UUID userId);

    // ✅ Find all members by role in an NGO
    List<NgoMember> findByNgoIdAndRoleAndIsDeletedFalse(
        UUID ngoId, String role);

    // ✅ Check if user has specific role in NGO
    boolean existsByNgoIdAndUserIdAndRoleAndIsDeleted(
        UUID ngoId, UUID userId, String role, boolean isDeleted);
}