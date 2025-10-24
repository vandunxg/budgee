package com.budgee.payload.request.group;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.budgee.enums.Currency;

public record GroupRequest(
        @Size(max = 255, message = "Group name must be at most 255 characters") String name,
        @NotNull(message = "Currency is required") Currency currency,
        List<GroupMemberRequest> groupMembers,
        @NotNull(message = "Start date is required") LocalDate startDate,
        @NotNull(message = "Start date is required") LocalDate endDate) {}
