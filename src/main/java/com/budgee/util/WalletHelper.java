package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.model.Wallet;
import com.budgee.service.WalletService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "WALLET_HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletHelper {

    WalletService walletService;

    SecurityHelper securityHelper;

    public Wallet getWalletByIdForOwner(UUID id) {
        log.info("[getWalletByIdForOwner]={}", id);

        Wallet wallet = walletService.getWalletById(id);
        securityHelper.checkIsOwner(wallet);
        return wallet;
    }
}
