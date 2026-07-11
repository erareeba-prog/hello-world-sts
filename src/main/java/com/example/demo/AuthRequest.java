package com.example.demo;

import lombok.Data;

@Data
public class AuthRequest {
    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;
}