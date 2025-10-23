package com.budgee.service;

import java.util.UUID;

import com.budgee.payload.request.group.GroupTransactionRequest;
import com.budgee.payload.response.group.GroupTransactionResponse;

public interface GroupTransactionService {

    GroupTransactionResponse createGroupTransaction(UUID groupID, GroupTransactionRequest request);
}
