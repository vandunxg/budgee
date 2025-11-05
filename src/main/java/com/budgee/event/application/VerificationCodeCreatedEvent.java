package com.budgee.event.application;

import java.util.UUID;

public record VerificationCodeCreatedEvent(UUID verificationCodeId) {}
