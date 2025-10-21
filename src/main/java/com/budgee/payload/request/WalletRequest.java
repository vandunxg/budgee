package com.budgee.payload.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

import com.budgee.enums.Currency;
import com.budgee.enums.WalletType;

public record WalletRequest(
        @NotBlank(message = "Name is required")
                @Size(max = 100, message = "Name must be at most 100 characters")
                String name,
        @NotNull(message = "Type is required") WalletType type,
        @NotNull(message = "Balance is required")
                @DecimalMin(value = "0.00", message = "Balance must be non-negative")
                BigDecimal balance,
        @NotNull(message = "Currency is required") Currency currency,
        @Size(max = 500, message = "Description must be at most 500 characters") String description,
        boolean isDefault,
        boolean isTotalIgnored) {}
