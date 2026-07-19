package com.example.demo;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AidConnect Platform API")
                .description(
                    "REST API for AidConnect Platform — " +
                    "manage NGOs, donations, needs, " +
                    "fulfillments and users.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("AidConnect")
                    .email("erareeba@gmail.com"))
                .license(new License()
                    .name("Amity License")))
            // JWT Bearer auth in Swagger UI
            .addSecurityItem(
                new SecurityRequirement()
                    .addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes(
                    "Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Enter JWT token from " +
                            "/api/auth/login")));
    }
}