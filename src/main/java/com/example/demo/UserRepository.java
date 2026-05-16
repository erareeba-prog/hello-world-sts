package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Find all users sorted by ID ascending
    List<User> findAllByOrderByIdAsc();

    // Find users by role sorted by ID ascending
    List<User> findByRoleOrderByIdAsc(Role role);
}