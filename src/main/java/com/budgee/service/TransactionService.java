package com.budgee.service;

import java.util.UUID;

import com.budgee.payload.request.TransactionRequest;
import com.budgee.payload.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest request);

    TransactionResponse updateTransaction(UUID id, TransactionRequest request);

    TransactionResponse getTransaction(UUID id);

    void deleteTransaction(UUID id);
}
