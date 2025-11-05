package com.budgee.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.budgee.enums.VerificationType;

public record SendVerificationRequest(
        @NotBlank(message = "Email must not be blank")
                @Email(message = "Invalid email format")
                @Size(max = 255, message = "Email must be at most 255 characters")
                String email,
        @NotNull(message = "Type must be not blank") VerificationType type) {}
