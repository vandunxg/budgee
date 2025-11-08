package com.budgee.payload.request;

import jakarta.validation.constraints.NotNull;

public record LogoutRequest(
        @NotNull(message = "Refresh token must be not null") String refreshToken) {}
