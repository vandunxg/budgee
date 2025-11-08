package com.budgee.payload.request.group;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AcceptJoinRequest(@NotNull(message = "userId must be not null") UUID userId) {}
