package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Register new user
    public Object register(AuthRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return "EMAIL_EXISTS";
        }

        // Check required fields
        if (request.getName() == null || request.getName().isEmpty()) {
            return "NAME_REQUIRED";
        }

        if (request.getRole() == null) {
            return "ROLE_REQUIRED";
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCreated_at(java.time.LocalDateTime.now());

        return userRepository.save(user);
    }

    // Login - authenticate user
    public Object authenticate(Long id, String rawPassword) {

        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return "NOT_FOUND";
        }

        User user = optionalUser.get();

        if (!encoder.matches(rawPassword, user.getPassword())) {
            return "WRONG_PASSWORD";
        }

        return user;
    }

    // Get all users sorted by ID
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    // Get users by role
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleOrderByIdAsc(role);
    }
    public java.util.Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    public Object updateProfile(Long userId, AuthRequest request) {

        Optional<User> optUser = userRepository.findById(userId);

        if (optUser.isEmpty()) return "NOT_FOUND";

        User user = optUser.get();

        // Update name if provided
        if (request.getName() != null && !request.getName().isEmpty())
            user.setName(request.getName());

        // Update email if provided and not taken
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail()))
                return "EMAIL_EXISTS";
            user.setEmail(request.getEmail());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty())
            user.setPassword(encoder.encode(request.getPassword()));

        return userRepository.save(user);
    }
}