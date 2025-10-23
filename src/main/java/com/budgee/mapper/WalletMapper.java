package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Wallet;
import com.budgee.payload.request.WalletRequest;
import com.budgee.payload.response.WalletResponse;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "name", source = "request.name")
    Wallet toWallet(WalletRequest request);

    @Mapping(target = "walletId", source = "wallet.id")
    WalletResponse toWalletResponse(Wallet wallet);
}
