package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.budgee.model.Wallet;
import com.budgee.payload.request.WalletRequest;
import com.budgee.payload.response.WalletResponse;

@Mapper
public interface WalletMapper {

    WalletMapper INSTANCE = Mappers.getMapper(WalletMapper.class);

    @Mapping(target = "name", source = "request.name")
    Wallet toWallet(WalletRequest request);

    @Mapping(target = "walletId", source = "wallet.id")
    WalletResponse toWalletResponse(Wallet wallet);
}
