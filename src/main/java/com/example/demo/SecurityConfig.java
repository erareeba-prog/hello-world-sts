package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/api/auth/send-otp",
                    "/api/auth/verify-otp",
                    "/api/auth/test-bcrypt",
                    "/api/dashboard/admin",
                    "/api/dashboard/ngo/*",
                    "/api/dashboard/donor/*",
                    "/api/notifications/**",
                    "/api/audit",
                    "/api/audit/**",
                    "/api/reports/**",
                 // ✅ Swagger UI
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    // ✅ WebSocket endpoints
                    "/ws/**",
                    "/ws/info/**",
                    // Static pages
                    "/login.html",
                    "/auth.html",
                    "/otp.html",
                    "/users.html",
                    "/dashboard.html",
                    "/ngos.html",
                    "/needs.html",
                    "/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}