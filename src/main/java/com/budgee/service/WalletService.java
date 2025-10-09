package com.budgee.service;

import java.util.List;
import java.util.UUID;

import com.budgee.payload.request.WalletRequest;
import com.budgee.payload.response.WalletResponse;

public interface WalletService {

    WalletResponse getWallet(UUID id);

    WalletResponse createWallet(WalletRequest request);

    WalletResponse updateWallet(UUID id, WalletRequest request);

    List<WalletResponse> getAllWallets();

    void deleteWallet(UUID id);
}
