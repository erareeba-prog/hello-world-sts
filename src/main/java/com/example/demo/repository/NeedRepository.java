package com.example.demo.repository;

import com.example.demo.model.Need;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NeedRepository extends JpaRepository<Need, UUID> {
    List<Need> findByIsDeletedFalse();
    List<Need> findByNgoIdAndIsDeletedFalse(UUID ngoId);
    List<Need> findByCategoryAndIsDeletedFalse(String category);
    List<Need> findByStatusAndIsDeletedFalse(String status);
    List<Need> findByUrgencyAndIsDeletedFalse(String urgency);
}