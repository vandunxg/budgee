package com.budgee.event.application;

import com.budgee.enums.VerificationType;

public record UserRegisteredEvent(String target, VerificationType type) {}
