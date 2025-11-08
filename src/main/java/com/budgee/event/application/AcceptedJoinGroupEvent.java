package com.budgee.event.application;

import java.util.UUID;

public record AcceptedJoinGroupEvent(UUID groupId, UUID userId) {}
