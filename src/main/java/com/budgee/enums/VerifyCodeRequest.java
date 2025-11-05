package com.budgee.enums;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(
        @NotBlank String code, @NotBlank @Email String target, VerificationType type) {}
