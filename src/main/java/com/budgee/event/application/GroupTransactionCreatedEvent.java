package com.budgee.event.application;

import java.util.UUID;

public record GroupTransactionCreatedEvent(UUID groupId, UUID transactionId) {}
