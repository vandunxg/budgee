package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.budgee.enums.Currency;
import com.budgee.enums.TransactionType;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.mapper.WalletMapper;
import com.budgee.model.User;
import com.budgee.model.Wallet;
import com.budgee.payload.request.WalletRequest;
import com.budgee.payload.response.WalletResponse;
import com.budgee.repository.WalletRepository;
import com.budgee.service.UserService;
import com.budgee.service.WalletService;
import com.budgee.util.Helpers;

/**
 * Implementation of the {@link WalletService} interface that provides services to handle
 * wallet-related operations, such as creating, updating, retrieving, and deleting wallets. This
 * class is responsible for managing wallet records in the persistence layer and enforcing ownership
 * rules for wallet access and modifications.
 *
 * <p>This service also interacts with the {@link UserService} to retrieve authenticated users and
 * with a helper utility to verify ownership of wallet entities.
 *
 * <p>Thread-safety is not guaranteed, as dependency injection ensures a single instance of this
 * class per application context.
 */
@Service
@RequiredArgsConstructor
@Slf4j(topic = "WALLET-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletServiceImpl implements WalletService {

    WalletRepository walletRepository;
    UserService userService;
    Helpers helpers;

    @Override
    public WalletResponse getWallet(UUID id) {
        log.info("[getWallet]={}", id);

        Wallet wallet = getWalletByIdForOwner(id);
        helpers.checkIsOwner(wallet);

        return WalletMapper.INSTANCE.toWalletResponse(wallet);
    }

    @Override
    public WalletResponse createWallet(WalletRequest request) {
        log.info("[createWallet]={}", request.toString());

        User authenticatedUser = userService.getCurrentUser();

        Wallet newWallet = WalletMapper.INSTANCE.toWallet(request);
        newWallet.setUser(authenticatedUser);

        if (request.isDefault()) {
            unsetDefaultAllWallets();
            newWallet.setIsDefault(Boolean.TRUE);
        }

        newWallet.setIsTotalIgnored(request.isTotalIgnored());

        newWallet.setCurrency(Currency.VND);

        log.warn("[createWallet] save to db");
        walletRepository.save(newWallet);

        return WalletMapper.INSTANCE.toWalletResponse(newWallet);
    }

    @Override
    public WalletResponse updateWallet(UUID id, WalletRequest request) {
        log.info("[updateWallet]={}", request);

        Wallet wallet = getWalletByIdForOwner(id);

        if (!wallet.getName().equals(request.name())) {
            wallet.setName(request.name());
        }

        if (!wallet.getBalance().equals(request.balance())) {
            wallet.setBalance(request.balance());
        }

        if (!wallet.getType().equals(request.type())) {
            wallet.setType(request.type());
        }

        if (wallet.getCurrency().equals(request.currency())) {
            wallet.setCurrency(request.currency());
        }

        if (!wallet.getIsDefault().equals(request.isDefault())) {
            wallet.setIsDefault(request.isDefault());
        }

        if (!wallet.getIsTotalIgnored().equals(request.isTotalIgnored())) {
            wallet.setIsTotalIgnored(request.isTotalIgnored());
        }

        log.warn("[updateWallet] update to db");
        walletRepository.save(wallet);

        return WalletMapper.INSTANCE.toWalletResponse(wallet);
    }

    // each user has about 5-10 wallets, so we shouldn't pageable a list of wallets
    @Override
    public List<WalletResponse> getAllWallets() {
        log.info("[getAllWallets]");

        User authenticatedUser = userService.getCurrentUser();

        List<Wallet> wallets = walletRepository.findAllByUser(authenticatedUser);

        return wallets.stream().map(WalletMapper.INSTANCE::toWalletResponse).toList();
    }

    @Override
    public void deleteWallet(UUID id) {
        log.info("[deleteWallet]={}", id);

        Wallet wallet = getWalletByIdForOwner(id);

        log.warn("[deleteWallet] delete from db");
        walletRepository.delete(wallet);
    }

    @Override
    public Wallet getWalletByIdForOwner(UUID id) {
        log.info("[getWalletByIdForOwner]={}", id);

        Wallet wallet = getWalletById(id);
        helpers.checkIsOwner(wallet);

        return wallet;
    }

    @Override
    public void updateWalletBalance(
            Wallet currentWallet,
            Wallet newWallet,
            BigDecimal currentAmount,
            BigDecimal newAmount,
            TransactionType currentType,
            TransactionType newType) {
        log.info("[updateWalletBalance] currentWallet={} newWallet={}", currentWallet, newWallet);

        boolean isSameWallet = currentWallet.getId().equals(newWallet.getId());

        if (isSameWallet) {
            updateBalanceSameWallet(currentWallet, currentAmount, newAmount, currentType, newType);
        } else {
            updateBalanceDifferentWallet(
                    currentWallet, newWallet, currentAmount, newAmount, currentType, newType);
        }
    }

    // PRIVATE FUNCTION

    void updateBalanceSameWallet(
            Wallet currentWallet,
            BigDecimal currentAmount,
            BigDecimal newAmount,
            TransactionType currentType,
            TransactionType newType) {
        log.info(
                "[updateBalanceSameWallet] currentType={} newType={} currentAmount={} newAmount={}",
                currentType,
                newType,
                currentAmount,
                newAmount);

        boolean isSameType = currentType.equals(newType);
        BigDecimal diffAmount = BigDecimal.ZERO;

        if (isSameType) {
            switch (currentType) {
                case EXPENSE -> diffAmount = currentAmount.subtract(newAmount);
                case INCOME -> diffAmount = newAmount.subtract(currentAmount);
            }

        } else {
            switch (currentType) {
                case EXPENSE -> diffAmount = currentAmount.add(newAmount);
                case INCOME -> diffAmount = currentAmount.add(newAmount).negate();
            }
        }

        if (diffAmount.signum() > 0) {
            currentWallet.increase(diffAmount);
            log.debug(
                    "[updateBalanceSameWallet] +{} -> newBalance={}",
                    diffAmount,
                    currentWallet.getBalance());
        } else if (diffAmount.signum() < 0) {
            currentWallet.decrease(diffAmount.abs());
            log.debug(
                    "[updateBalanceSameWallet] -{} -> newBalance={}",
                    diffAmount.abs(),
                    currentWallet.getBalance());
        } else {
            log.debug("[updateBalanceSameWallet] No change to balance");
        }

        walletRepository.save(currentWallet);
    }

    private void updateBalanceDifferentWallet(
            Wallet currentWallet,
            Wallet newWallet,
            BigDecimal currentAmount,
            BigDecimal newAmount,
            TransactionType currentType,
            TransactionType newType) {
        log.info(
                "[updateBalanceDifferentWallet] currentType={} newType={} currentAmount={} newAmount={}",
                currentType,
                newType,
                currentAmount,
                newAmount);

        switch (currentType) {
            case EXPENSE -> currentWallet.increase(currentAmount);
            case INCOME -> currentWallet.decrease(currentAmount);
        }

        switch (newType) {
            case EXPENSE -> newWallet.decrease(newAmount);
            case INCOME -> newWallet.increase(newAmount);
        }

        log.debug(
                "[updateBalanceDifferentWallet] currentWallet={} newBalance={} | newWallet={} newBalance={}",
                currentWallet.getId(),
                currentWallet.getBalance(),
                newWallet.getId(),
                newWallet.getBalance());

        walletRepository.saveAll(List.of(currentWallet, newWallet));
    }

    void unsetDefaultAllWallets() {
        log.info("[unsetDefaultAllWallets]");

        List<Wallet> wallets = walletRepository.findAll();

        if (!wallets.isEmpty()) {
            wallets.forEach(x -> x.setIsDefault(Boolean.FALSE));

            log.warn("[unsetDefaultAllWallets] save to db");
            walletRepository.saveAll(wallets);
        }
    }

    Wallet getWalletById(UUID id) {
        log.info("[getWalletById]={}", id);

        return walletRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
    }
}
