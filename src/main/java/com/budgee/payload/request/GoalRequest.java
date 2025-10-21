package com.budgee.payload.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GoalRequest(
        @NotNull(message = "Target amount is required")
                @DecimalMin(value = "0.00", message = "Target amount must be non-negative")
                BigDecimal targetAmount,
        @Size(max = 255, message = "Goal name must be at most 255 characters") String name,
        @NotNull(message = "Category is required") List<UUID> categories,
        @NotNull(message = "Wallet is required") List<UUID> wallets,
        @NotNull(message = "Start date is required") LocalDate startDate,
        @NotNull(message = "Start date is required") LocalDate endDate) {}
