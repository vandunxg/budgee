package com.budgee.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.budgee.enums.VerificationType;

public record VerificationRequest(
        @NotBlank(message = "Email must not be blank")
                @Email(message = "Invalid email format")
                @Size(max = 255, message = "Email must be at most 255 characters")
                String email,
        String code,
        @NotNull(message = "Verification type must be not null") VerificationType type) {}
