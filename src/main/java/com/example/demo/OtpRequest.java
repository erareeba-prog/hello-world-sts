package com.example.demo;

import lombok.Data;

@Data
public class OtpRequest {
    private Long userId;
    private String phone;
    private String otp;
}