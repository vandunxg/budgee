package com.budgee.service;

import java.util.List;
import java.util.UUID;

import com.budgee.model.Category;
import com.budgee.model.Transaction;
import com.budgee.payload.request.TransactionRequest;
import com.budgee.payload.request.group.GroupTransactionRequest;
import com.budgee.payload.response.TransactionResponse;
import com.budgee.payload.response.group.GroupTransactionResponse;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest request);

    GroupTransactionResponse createGroupTransaction(GroupTransactionRequest request, UUID groupId);

    TransactionResponse updateTransaction(UUID id, TransactionRequest request);

    TransactionResponse getTransaction(UUID id);

    List<Transaction> getTransactionByCategory(Category category);

    void deleteTransaction(UUID id);

    Transaction getTransactionById(UUID id);
}
