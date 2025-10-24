package com.budgee.mapper;

import com.budgee.model.Category;
import com.budgee.model.User;
import com.budgee.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Transaction;
import com.budgee.payload.request.TransactionRequest;
import com.budgee.payload.response.TransactionResponse;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "wallet", source = "wallet")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "type", ignore = true)
    Transaction toTransaction(TransactionRequest request, Wallet wallet, Category category, User user);

    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "categoryId", source = "category.id")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
