package com.budgee.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Budgee API Documentation",
                        version = "1.0.0",
                        description = "API reference for Budgee backend services",
                        contact = @Contact(name = "vandunxg", email = "vandunxg@example.com")),
        servers = {@Server(url = "http://localhost:8080", description = "Local Server")})
public class SwaggerConfig {}
