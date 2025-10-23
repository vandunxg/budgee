package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Transaction;
import com.budgee.payload.request.TransactionRequest;
import com.budgee.payload.response.TransactionResponse;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    Transaction toTransaction(TransactionRequest request);

    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "categoryId", source = "category.id")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
