package com.budgee.payload.request.group;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record GroupMemberRequest(
        @Size(max = 255, message = "Group member name must be at most 255 characters")
                String memberName,
        @DecimalMin(value = "0.00", message = "Prepaid amount must be non-negative")
                BigDecimal advanceAmount,
        UUID userId) {}
