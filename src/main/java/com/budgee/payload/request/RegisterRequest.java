package com.budgee.payload.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Full name must not be blank")
                @Size(max = 255, message = "Full name must be at most 255 characters")
                String fullName,
        @NotBlank(message = "Email must not be blank")
                @Email(message = "Invalid email format")
                @Size(max = 255, message = "Email must be at most 255 characters")
                String email,
        @NotBlank(message = "Password must not be blank")
                @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
                @Pattern(
                        regexp =
                                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                        message =
                                "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
                String password,
        @NotBlank(message = "Confirm password must not be blank") String confirmPassword,
        @NotBlank(message = "Platform must not be blank")
                @Pattern(
                        regexp = "^(WEB|IOS|ANDROID)$",
                        message = "Platform must be WEB, IOS, or ANDROID")
                String platform,
        @NotBlank(message = "Device token must not be blank")
                @Size(max = 255, message = "Device token must be at most 255 characters")
                String tokenDevice) {}
