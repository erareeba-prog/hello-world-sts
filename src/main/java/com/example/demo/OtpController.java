package com.example.demo;

import com.example.demo.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import com.example.demo.UserRepository;
import com.example.demo.User;
import com.example.demo.OtpRequest;

@RestController
@RequestMapping("/api/auth")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    // POST /api/auth/send-otp
    @PostMapping("/send-otp")
    public ResponseEntity<Object> sendOtp(
            @RequestBody OtpRequest request) {

        Optional<User> optUser =
            userRepository.findById(request.getUserId());

        if (optUser.isEmpty())
            return ResponseEntity.status(404)
                .body("❌ User not found!");

        User user = optUser.get();

        if (request.getPhone() == null ||
                request.getPhone().isEmpty())
            return ResponseEntity.status(400)
                .body("❌ Phone number is required!");

        if (otpService.otpExists(
                request.getUserId(), request.getPhone())) {
            Long ttl = otpService.getOtpTtl(
                request.getUserId(), request.getPhone());
            return ResponseEntity.status(429)
                .body("⚠️ OTP already sent! Please wait "
                    + ttl + " seconds before requesting again.");
        }

        if (user.getPhone() == null ||
                user.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
            userRepository.save(user);
        }

        String otp = otpService.generateOtp(
            request.getUserId(), request.getPhone());

        System.out.println("OTP for user "
            + request.getUserId() + ": " + otp);

        // ✅ OTP shown in response
        return ResponseEntity.ok(Map.of(
            "message", "✅ OTP sent successfully!",
            "phone", request.getPhone(),
            "expiresIn", "5 minutes",
            "otp", otp
        ));
    }

    // POST /api/auth/verify-otp
    @PostMapping("/verify-otp")
    public ResponseEntity<Object> verifyOtp(
            @RequestBody OtpRequest request) {

        Optional<User> optUser =
            userRepository.findById(request.getUserId());

        if (optUser.isEmpty())
            return ResponseEntity.status(404)
                .body("❌ User not found!");

        if (request.getPhone() == null ||
                request.getPhone().isEmpty())
            return ResponseEntity.status(400)
                .body("❌ Phone number is required!");

        if (request.getOtp() == null ||
                request.getOtp().isEmpty())
            return ResponseEntity.status(400)
                .body("❌ OTP is required!");

        boolean isValid = otpService.validateOtp(
            request.getUserId(),
            request.getPhone(),
            request.getOtp()
        );

        if (!isValid)
            return ResponseEntity.status(400)
                .body("❌ Invalid or expired OTP! " +
                    "Please request a new OTP.");

        User user = optUser.get();
        user.setPhoneVerified(true);
        user.setPhoneVerifiedAt(LocalDateTime.now());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message", "✅ Phone verified successfully!",
            "userId", user.getId(),
            "phone", request.getPhone(),
            "phoneVerified", true,
            "verifiedAt", LocalDateTime.now().toString()
        ));
    }
}