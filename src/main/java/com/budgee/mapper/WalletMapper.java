package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.enums.Currency;
import com.budgee.model.User;
import com.budgee.model.Wallet;
import com.budgee.payload.request.WalletRequest;
import com.budgee.payload.response.WalletResponse;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "currency", source = "currency")
    Wallet toWallet(WalletRequest request, User user, Currency currency);

    @Mapping(target = "walletId", source = "wallet.id")
    WalletResponse toWalletResponse(Wallet wallet);
}
