package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.budgee.enums.Currency;
import com.budgee.enums.TransactionType;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.mapper.WalletMapper;
import com.budgee.model.Transaction;
import com.budgee.model.User;
import com.budgee.model.Wallet;
import com.budgee.payload.request.WalletRequest;
import com.budgee.payload.response.WalletResponse;
import com.budgee.repository.TransactionRepository;
import com.budgee.repository.WalletRepository;
import com.budgee.service.UserService;
import com.budgee.service.WalletService;
import com.budgee.util.SecurityHelper;

/**
 * Implementation of {@link WalletService} that handles CRUD operations and domain-level balance
 * adjustments for wallets.
 */
@Service
@RequiredArgsConstructor
@Slf4j(topic = "WALLET-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletServiceImpl implements WalletService {

    TransactionRepository transactionRepository;
    WalletRepository walletRepository;

    UserService userService;

    WalletMapper walletMapper;

    SecurityHelper securityHelper;

    @Override
    public WalletResponse getWallet(UUID id) {
        log.info("[getWallet] id={}", id);
        Wallet wallet = this.getWalletByIdForOwner(id);
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    public List<WalletResponse> getAllWallets() {
        log.info("[getAllWallets]");
        List<Wallet> wallets = getAllWalletsByUser();
        return wallets.stream().map(walletMapper::toWalletResponse).toList();
    }

    @Override
    @Transactional
    public WalletResponse createWallet(WalletRequest request) {
        log.info("[createWallet] request={}", request);

        User user = userService.getCurrentUser();
        Wallet wallet = walletMapper.toWallet(request);
        wallet.setUser(user);
        wallet.setCurrency(request.currency() != null ? request.currency() : Currency.VND);
        wallet.setIsTotalIgnored(request.isTotalIgnored());

        if (request.isDefault()) {
            unsetDefaultAllWallets();
            wallet.setIsDefault(Boolean.TRUE);
        }

        walletRepository.save(wallet);
        log.debug("[createWallet] saved wallet={} balance={}", wallet.getId(), wallet.getBalance());

        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse updateWallet(UUID id, WalletRequest request) {
        log.info("[updateWallet] id={} request={}", id, request);

        Wallet wallet = this.getWalletByIdForOwner(id);

        if (!wallet.getName().equals(request.name())) wallet.setName(request.name());
        if (!wallet.getBalance().equals(request.balance())) wallet.setBalance(request.balance());
        if (!wallet.getType().equals(request.type())) wallet.setType(request.type());
        if (!wallet.getCurrency().equals(request.currency()))
            wallet.setCurrency(request.currency());
        if (!wallet.getIsDefault().equals(request.isDefault()))
            wallet.setIsDefault(request.isDefault());
        if (!wallet.getIsTotalIgnored().equals(request.isTotalIgnored()))
            wallet.setIsTotalIgnored(request.isTotalIgnored());

        walletRepository.save(wallet);
        log.debug(
                "[updateWallet] updated wallet={} balance={}", wallet.getId(), wallet.getBalance());
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    @Transactional
    public void deleteWallet(UUID id) {
        log.info("[deleteWallet] id={}", id);

        Wallet wallet = this.getWalletByIdForOwner(id);
        log.warn("[deleteWallet] removing related transactions walletId={}", wallet.getId());
        transactionRepository.deleteAllByWalletAndUser(wallet, userService.getCurrentUser());

        walletRepository.delete(wallet);
        log.warn("[deleteWallet] deleted walletId={}", wallet.getId());
    }

    @Override
    @Transactional
    public void applyTransaction(Wallet wallet, Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        log.info(
                "[applyTransaction] wallet={} type={} amount={}",
                wallet.getId(),
                transaction.getType(),
                amount);

        switch (transaction.getType()) {
            case EXPENSE -> wallet.decrease(amount);
            case INCOME -> wallet.increase(amount);
            default -> throw new IllegalArgumentException(
                    "Unsupported type: " + transaction.getType());
        }

        walletRepository.save(wallet);
        log.debug("[applyTransaction] newBalance={}", wallet.getBalance());
    }

    @Override
    @Transactional
    public void reverseTransaction(Wallet wallet, Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        log.info(
                "[reverseTransaction] wallet={} type={} amount={}",
                wallet.getId(),
                transaction.getType(),
                amount);

        switch (transaction.getType()) {
            case EXPENSE -> wallet.increase(amount);
            case INCOME -> wallet.decrease(amount);
            default -> throw new IllegalArgumentException(
                    "Unsupported type: " + transaction.getType());
        }

        walletRepository.save(wallet);
        log.debug("[reverseTransaction] newBalance={}", wallet.getBalance());
    }

    @Override
    @Transactional
    public void updateBalanceForTransactionUpdate(
            Wallet oldWallet,
            Wallet newWallet,
            BigDecimal oldAmount,
            BigDecimal newAmount,
            TransactionType oldType,
            TransactionType newType) {

        log.info(
                """
                        [updateBalanceForTransactionUpdate]
                        oldWallet={} newWallet={}
                        oldAmount={} newAmount={}
                        oldType={} newType={}
                        """,
                oldWallet.getId(),
                newWallet.getId(),
                oldAmount,
                newAmount,
                oldType,
                newType);

        boolean sameWallet = oldWallet.getId().equals(newWallet.getId());

        if (sameWallet) {
            if (oldType.equals(newType)) {
                BigDecimal diff =
                        switch (oldType) {
                            case EXPENSE -> oldAmount.subtract(newAmount);
                            case INCOME -> newAmount.subtract(oldAmount);
                            default -> throw new NotFoundException(
                                    ErrorCode.INVALID_TRANSACTION_TYPE);
                        };
                adjustWalletBalance(oldWallet, diff);
            } else {

                reverseTransaction(
                        oldWallet, Transaction.builder().amount(oldAmount).type(oldType).build());

                applyTransaction(
                        oldWallet, Transaction.builder().amount(newAmount).type(newType).build());
            }
            walletRepository.save(oldWallet);
        } else {

            reverseTransaction(
                    oldWallet, Transaction.builder().amount(oldAmount).type(oldType).build());

            applyTransaction(
                    newWallet, Transaction.builder().amount(newAmount).type(newType).build());

            walletRepository.saveAll(List.of(oldWallet, newWallet));
        }
    }

    @Override
    public Wallet getWalletById(UUID id) {
        log.info("[getWalletById]={}", id);

        return walletRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
    }

    // -------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------

    Wallet getWalletByIdForOwner(UUID id) {
        log.info("[getWalletByIdForOwner]={}", id);

        Wallet wallet = getWalletById(id);
        securityHelper.checkIsOwner(wallet);
        return wallet;
    }

    void adjustWalletBalance(Wallet wallet, BigDecimal diff) {
        if (diff.signum() > 0) {
            wallet.increase(diff);
            log.debug("[adjustWalletBalance] +{} -> {}", diff, wallet.getBalance());
        } else if (diff.signum() < 0) {
            wallet.decrease(diff.abs());
            log.debug("[adjustWalletBalance] -{} -> {}", diff.abs(), wallet.getBalance());
        } else {
            log.trace("[adjustWalletBalance] no change");
        }
    }

    List<Wallet> getAllWalletsByUser() {
        User user = userService.getCurrentUser();
        return walletRepository.findAllByUser(user);
    }

    void unsetDefaultAllWallets() {
        List<Wallet> wallets = getAllWalletsByUser();
        if (!wallets.isEmpty()) {
            wallets.forEach(w -> w.setIsDefault(false));
            walletRepository.saveAll(wallets);
        }
    }
}
