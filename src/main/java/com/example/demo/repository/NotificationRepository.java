package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    // Paginated notifications for a user
    Page<Notification> findByUserIdOrderByCreatedAtDesc(
        Long userId, Pageable pageable);

    // Last 10 notifications
    java.util.List<Notification>
        findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    // Unread count
    long countByUserIdAndIsReadFalse(Long userId);

    // Mark all as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true " +
           "WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);
}