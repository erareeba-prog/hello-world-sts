package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByOrderByIdAsc();

    List<User> findByRoleOrderByIdAsc(Role role);

    boolean existsByIdAndIsDeletedFalseAndIsActiveTrue(
        Long id);

    // ✅ Count total users
    @Query("SELECT COUNT(u) FROM User u " +
           "WHERE u.isDeleted = false")
    long countActiveUsers();
}