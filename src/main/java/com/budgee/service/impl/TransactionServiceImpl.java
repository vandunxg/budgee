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

import com.budgee.enums.TransactionType;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.exception.ValidationException;
import com.budgee.mapper.TransactionMapper;
import com.budgee.model.*;
import com.budgee.payload.request.TransactionRequest;
import com.budgee.payload.response.TransactionResponse;
import com.budgee.repository.TransactionRepository;
import com.budgee.repository.WalletRepository;
import com.budgee.service.*;
import com.budgee.service.lookup.CategoryLookup;
import com.budgee.service.lookup.WalletLookup;
import com.budgee.util.AuthContext;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    TransactionRepository transactionRepository;
    WalletRepository walletRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    WalletDomainService walletDomainService;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    TransactionMapper transactionMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    AuthContext authContext;

    // -------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------
    WalletLookup walletLookup;
    CategoryLookup categoryLookup;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("[createTransaction] request={}", request);

        Wallet wallet = walletLookup.getWalletForCurrentUser(request.walletId());
        Category category = categoryLookup.getCategoryForCurrentUser(request.categoryId());
        User user = authContext.getAuthenticatedUser();

        Transaction transaction = transactionMapper.toTransaction(request, wallet, category, user);

        ensureTransactionTypeMatchesCategory(category.getType(), request.type());

        walletDomainService.applyTransaction(wallet, transaction);

        log.debug(
                "[createTransaction] update wallet when create transactionId={}",
                transaction.getId());
        walletRepository.save(wallet);

        log.debug("[createTransaction] saving transaction...");
        transactionRepository.save(transaction);

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(UUID id, TransactionRequest request) {
        log.info("[updateTransaction] id={} request={}", id, request);

        Transaction transaction = getTransactionById(id);
        Category newCategory = categoryLookup.getCategoryForCurrentUser(request.categoryId());
        Wallet newWallet = walletLookup.getWalletForCurrentUser(request.walletId());
        Wallet oldWallet = transaction.getWallet();

        BigDecimal oldAmount = transaction.getAmount();
        BigDecimal newAmount = request.amount();
        TransactionType oldType = transaction.getType();
        TransactionType newType = request.type();

        authContext.checkIsOwner(transaction);
        ensureTransactionTypeMatchesCategory(newCategory.getType(), newType);

        applyTransactionChanges(transaction, request, newCategory, newWallet);

        walletDomainService.updateBalanceForTransactionUpdate(
                oldWallet, newWallet, oldAmount, newAmount, oldType, newType);
        log.debug(
                "[createTransaction] save wallet when update transactionId={}",
                transaction.getId());
        walletRepository.saveAll(List.of(oldWallet, newWallet));

        log.debug("[updateTransaction] updated successfully");
        transactionRepository.save(transaction);

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse getTransaction(UUID id) {
        log.info("[getTransaction] id={}", id);

        Transaction transaction = getTransactionById(id);
        authContext.checkIsOwner(transaction);

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    public List<Transaction> getTransactionByCategory(Category category) {
        log.info("[getTransactionByCategory] category={}", category);

        return transactionRepository.getTransactionsByCategory(category);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID id) {
        log.info("[deleteTransaction] id={}", id);

        Transaction transaction = getTransactionById(id);
        Wallet wallet = transaction.getWallet();
        authContext.checkIsOwner(transaction);

        walletDomainService.reverseTransaction(wallet, transaction);
        log.debug(
                "[deleteTransaction] update wallet when delete transactionId={}",
                transaction.getId());
        walletRepository.save(wallet);

        log.warn("[deleteTransaction] deleted transaction id={}", id);
        transactionRepository.delete(transaction);
    }

    public Transaction getTransactionById(UUID id) {
        log.info("[getTransactionById] id={}", id);

        return transactionRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------
    void applyTransactionChanges(
            Transaction transaction, TransactionRequest request, Category category, Wallet wallet) {
        log.info("[applyTransactionChanges]");

        transaction.setCategory(category);
        transaction.setWallet(wallet);
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setDate(request.date());
        transaction.setTime(request.time());
        transaction.setNote(request.note());
    }

    void ensureTransactionTypeMatchesCategory(
            TransactionType typeOfCategory, TransactionType typeOfTransaction) {
        log.info(
                "[ensureTransactionTypeMatchesCategory] typeOfCategory={} typeOfTransaction={}",
                typeOfCategory,
                typeOfTransaction);

        if (!typeOfCategory.equals(typeOfTransaction)) {
            log.error(
                    "[ensureTransactionTypeMatchesCategory] typeOfCategory not equal typeOfTransaction");

            throw new ValidationException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }
}
