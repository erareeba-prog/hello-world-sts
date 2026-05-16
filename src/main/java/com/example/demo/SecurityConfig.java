package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/*.html").permitAll()
                .requestMatchers("/users").permitAll()
                .requestMatchers("/users/**").permitAll()
                .requestMatchers("/api/ngos/**").permitAll()
                .requestMatchers("/api/needs/**").permitAll()
                .requestMatchers("/api/donations/**").permitAll()
                .requestMatchers("/api/fulfillments/**").permitAll()
                .requestMatchers("/api/audit/**").permitAll()
                .requestMatchers("/api/providers/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}