package com.budgee.payload.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.budgee.enums.TransactionType;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
                @DecimalMin(value = "0.00", message = "Amount must be non-negative")
                BigDecimal amount,
        @NotNull(message = "Type is required") TransactionType type,
        @NotNull(message = "Wallet is required") UUID walletId,
        @NotNull(message = "Categories is required") UUID categoryId,
        @NotNull(message = "Date is required") LocalDate date,
        @NotNull(message = "Time is required") LocalTime time,
        String note) {}
