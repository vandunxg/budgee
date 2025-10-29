package com.budgee.event.application;

import java.util.UUID;

/**
 * Application event fired when a Category is deleted. Used by other domains (Goal, Transaction,
 * etc.) to clean up related data.
 */
public record WalletDeletedEvent(UUID walletId, UUID ownerId) {}
