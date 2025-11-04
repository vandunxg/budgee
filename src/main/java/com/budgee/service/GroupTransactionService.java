package com.budgee.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.budgee.payload.request.group.GroupTransactionRequest;
import com.budgee.payload.response.group.GroupTransactionResponse;

public interface GroupTransactionService {

    CompletableFuture<GroupTransactionResponse> createGroupTransaction(
            UUID groupID, GroupTransactionRequest request);

    GroupTransactionResponse getGroupTransaction(UUID groupId, UUID transactionId);
}
