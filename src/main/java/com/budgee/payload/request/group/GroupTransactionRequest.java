package com.budgee.payload.request.group;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.enums.TransactionType;

public record GroupTransactionRequest(
        @NotNull(message = "Amount is required")
                @DecimalMin(value = "0.00", message = "Amount must be non-negative")
                BigDecimal amount,
        @NotNull(message = "Note is required") String note,
        @NotNull(message = "Type is required") TransactionType type,
        @NotNull(message = "Member ID is required") UUID memberId,
        @NotNull(message = "Expense source is required") GroupExpenseSource groupExpenseSource,
        @NotNull(message = "Date is required") LocalDate date,
        @NotNull(message = "Time is required") LocalTime time) {}
