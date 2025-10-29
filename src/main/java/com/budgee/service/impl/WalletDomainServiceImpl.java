package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.budgee.enums.TransactionType;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.model.Transaction;
import com.budgee.model.Wallet;
import com.budgee.service.WalletDomainService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "WALLET-DOMAIN-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletDomainServiceImpl implements WalletDomainService {

    @Override
    public void applyTransaction(Wallet wallet, Transaction transaction) {
        log.info(
                "[applyTransaction] walletId={} transactionId={}",
                wallet.getId(),
                transaction.getId());

        switch (transaction.getType()) {
            case EXPENSE -> wallet.decrease(transaction.getAmount());
            case INCOME -> wallet.increase(transaction.getAmount());
            default -> throw new IllegalArgumentException(
                    "Unsupported type: " + transaction.getType());
        }
    }

    @Override
    public void reverseTransaction(Wallet wallet, Transaction transaction) {
        log.info(
                "[reverseTransaction] walletId={} transactionId={}",
                wallet.getId(),
                transaction.getId());

        switch (transaction.getType()) {
            case EXPENSE -> wallet.increase(transaction.getAmount());
            case INCOME -> wallet.decrease(transaction.getAmount());
            default -> throw new IllegalArgumentException(
                    "Unsupported type: " + transaction.getType());
        }
    }

    public void updateBalanceForTransactionUpdate(
            Wallet oldWallet,
            Wallet newWallet,
            BigDecimal oldAmount,
            BigDecimal newAmount,
            TransactionType oldType,
            TransactionType newType) {

        boolean sameWallet = oldWallet.getId().equals(newWallet.getId());

        if (sameWallet) {
            handleSameWallet(oldWallet, oldAmount, newAmount, oldType, newType);
        } else {
            reverseTransaction(oldWallet, Transaction.of(oldAmount, oldType));
            applyTransaction(newWallet, Transaction.of(newAmount, newType));
        }
    }

    void handleSameWallet(
            Wallet wallet,
            BigDecimal oldAmount,
            BigDecimal newAmount,
            TransactionType oldType,
            TransactionType newType) {

        if (oldType.equals(newType)) {
            BigDecimal diff = calculateDiff(oldType, oldAmount, newAmount);
            adjust(wallet, diff);
        } else {
            reverseTransaction(wallet, Transaction.of(oldAmount, oldType));
            applyTransaction(wallet, Transaction.of(newAmount, newType));
        }
    }

    BigDecimal calculateDiff(TransactionType type, BigDecimal oldAmount, BigDecimal newAmount) {
        return switch (type) {
            case EXPENSE -> oldAmount.subtract(newAmount);
            case INCOME -> newAmount.subtract(oldAmount);
            default -> throw new ValidationException(ErrorCode.INVALID_TRANSACTION_TYPE);
        };
    }

    void adjust(Wallet wallet, BigDecimal diff) {
        if (diff.signum() > 0) wallet.increase(diff);
        else if (diff.signum() < 0) wallet.decrease(diff.abs());
    }
}
