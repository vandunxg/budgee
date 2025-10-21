package com.budgee.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.budgee.enums.TransactionType;

public record CategoryRequest(
        @Size(max = 255, message = "Category name must be at most 255 characters") String name,
        @NotNull(message = "Transaction type is required") TransactionType type,
        @Size(max = 50, message = "Color code must be at most 50 characters") String color,
        @Size(max = 100, message = "Icon name must be at most 100 characters") String icon) {}
