package com.budgee.util;

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

@Component
@RequiredArgsConstructor
@Slf4j(topic = "WALLET-HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletHelper {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    WalletRepository walletRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    SecurityHelper securityHelper;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    public Wallet getWalletByIdForOwner(UUID id) {
        log.info("[getWalletByIdForOwner]={}", id);

        Wallet wallet =
                walletRepository
                        .findById(id)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));

        securityHelper.checkIsOwner(wallet);

        return wallet;
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

}
