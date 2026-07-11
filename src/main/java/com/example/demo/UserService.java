package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder encoder =
        new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleOrderByIdAsc(role);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Object authenticate(Long id, String rawPassword) {
        Optional<User> optionalUser =
            userRepository.findById(id);
        if (optionalUser.isEmpty()) return "NOT_FOUND";
        User user = optionalUser.get();
        if (!encoder.matches(rawPassword, user.getPassword()))
            return "WRONG_PASSWORD";
        return user;
    }

    public Object register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            return "EMAIL_EXISTS";
        if (request.getName() == null ||
                request.getName().isEmpty())
            return "NAME_REQUIRED";
        if (request.getRole() == null)
            return "ROLE_REQUIRED";
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
            encoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCreated_at(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Object updateProfile(Long userId,
            AuthRequest request) {
        Optional<User> optUser =
            userRepository.findById(userId);
        if (optUser.isEmpty()) return "NOT_FOUND";
        User user = optUser.get();
        if (request.getName() != null &&
                !request.getName().isEmpty())
            user.setName(request.getName());
        if (request.getEmail() != null &&
                !request.getEmail().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(
                    request.getEmail()))
                return "EMAIL_EXISTS";
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null &&
                !request.getPassword().isEmpty())
            user.setPassword(
                encoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
}