package com.budgee.service;

import java.math.BigDecimal;

import com.budgee.enums.TransactionType;
import com.budgee.model.Transaction;
import com.budgee.model.Wallet;

public interface WalletDomainService {

    void applyTransaction(Wallet wallet, Transaction transaction);

    void reverseTransaction(Wallet wallet, Transaction transaction);

    void updateBalanceForTransactionUpdate(
            Wallet oldWallet,
            Wallet newWallet,
            BigDecimal oldAmount,
            BigDecimal newAmount,
            TransactionType oldType,
            TransactionType newType);
}
