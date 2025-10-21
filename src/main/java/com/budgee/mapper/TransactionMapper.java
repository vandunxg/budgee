package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.budgee.model.Transaction;
import com.budgee.payload.request.TransactionRequest;
import com.budgee.payload.response.TransactionResponse;

@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    Transaction toTransaction(TransactionRequest request);

    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "categoryId", source = "category.id")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
