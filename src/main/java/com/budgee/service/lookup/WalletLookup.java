package com.budgee.service.lookup;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.model.Wallet;
import com.budgee.repository.WalletRepository;
import com.budgee.util.AuthContext;

@Component
@Slf4j(topic = "WALLET-LOOKUP")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletLookup {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    WalletRepository walletRepository;

    // -------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------
    AuthContext authContext;

    public Wallet getWalletById(UUID id) {
        log.info("[getWalletById]={}", id);

        return walletRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
    }

    public Wallet getWalletForCurrentUser(UUID walletId) {
        log.info("[getWalletForCurrentUser] categoryId={}", walletId);

        Wallet wallet = this.getWalletById(walletId);
        authContext.checkIsOwner(wallet);

        return wallet;
    }
}
