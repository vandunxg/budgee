package com.budgee.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.budgee.enums.TransactionType;
import com.budgee.model.Wallet;
import com.budgee.payload.request.WalletRequest;
import com.budgee.payload.response.WalletResponse;

public interface WalletService {

    WalletResponse getWallet(UUID id);

    WalletResponse createWallet(WalletRequest request);

    WalletResponse updateWallet(UUID id, WalletRequest request);

    List<WalletResponse> getAllWallets();

    void deleteWallet(UUID id);

    Wallet getWalletByIdForOwner(UUID id);

    void updateWalletBalance(
            Wallet currentWallet,
            Wallet newWallet,
            BigDecimal currentAmount,
            BigDecimal newAmount,
            TransactionType currentType,
            TransactionType newType);
}
