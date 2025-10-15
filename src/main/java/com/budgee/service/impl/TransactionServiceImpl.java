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
import com.budgee.service.CategoryService;
import com.budgee.service.TransactionService;
import com.budgee.service.UserService;
import com.budgee.service.WalletService;
import com.budgee.util.Helpers;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    TransactionRepository transactionRepository;
    WalletService walletService;
    CategoryService categoryService;
    UserService userService;
    Helpers helpers;

    // -----------------------------------------------------
    // CREATE
    // -----------------------------------------------------
    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("[createTransaction] request={}", request);

        Transaction transaction = TransactionMapper.INSTANCE.toTransaction(request);

        Wallet wallet = walletService.getWalletByIdForOwner(request.walletId());
        Category category = categoryService.getCategoryByIdForOwner(request.categoryId());
        User user = userService.getCurrentUser();

        checkNewTypeOfTransactionWithTypeOfCategory(category.getType(), request.type());

        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setUser(user);

        walletService.applyTransaction(wallet, transaction);

        log.debug("[createTransaction] saving transaction...");
        transactionRepository.save(transaction);

        return toTransactionResponse(transaction);
    }

    // -----------------------------------------------------
    // UPDATE
    // -----------------------------------------------------
    @Override
    @Transactional
    public TransactionResponse updateTransaction(UUID id, TransactionRequest request) {
        log.info("[updateTransaction] id={} request={}", id, request);

        Transaction transaction = getTransactionById(id);
        helpers.checkIsOwner(transaction);

        Wallet oldWallet = transaction.getWallet();
        Wallet newWallet = walletService.getWalletByIdForOwner(request.walletId());

        BigDecimal oldAmount = transaction.getAmount();
        BigDecimal newAmount = request.amount();
        TransactionType oldType = transaction.getType();
        TransactionType newType = request.type();

        Category newCategory = categoryService.getCategoryByIdForOwner(request.categoryId());
        checkNewTypeOfTransactionWithTypeOfCategory(newCategory.getType(), newType);

        walletService.updateBalanceForTransactionUpdate(
                oldWallet, newWallet, oldAmount, newAmount, oldType, newType);

        transaction.setWallet(newWallet);
        transaction.setCategory(newCategory);
        transaction.setAmount(newAmount);
        transaction.setType(newType);
        transaction.setDate(request.date());
        transaction.setTime(request.time());
        transaction.setNote(request.note());

        transactionRepository.save(transaction);
        log.debug("[updateTransaction] updated successfully");

        return toTransactionResponse(transaction);
    }

    // -----------------------------------------------------
    // GET
    // -----------------------------------------------------
    @Override
    public TransactionResponse getTransaction(UUID id) {
        log.info("[getTransaction] id={}", id);

        Transaction transaction = getTransactionById(id);
        helpers.checkIsOwner(transaction);
        return toTransactionResponse(transaction);
    }

    @Override
    public List<Transaction> getTransactionByCategory(Category category) {
        log.info("[getTransactionByCategory] category={}", category);

        return transactionRepository.getTransactionsByCategory(category);
    }

    // -----------------------------------------------------
    // DELETE
    // -----------------------------------------------------
    @Override
    @Transactional
    public void deleteTransaction(UUID id) {
        log.info("[deleteTransaction] id={}", id);

        Transaction transaction = getTransactionById(id);
        helpers.checkIsOwner(transaction);

        walletService.reverseTransaction(transaction.getWallet(), transaction);

        transactionRepository.delete(transaction);
        log.warn("[deleteTransaction] deleted transaction id={}", id);
    }

    // -----------------------------------------------------
    // PRIVATE HELPERS
    // -----------------------------------------------------
    void checkNewTypeOfTransactionWithTypeOfCategory(
            TransactionType typeOfCategory, TransactionType typeOfTransaction) {
        log.info(
                "[checkNewTypeOfTransactionWithTypeOfCategory] typeOfCategory={} typeOfTransaction={}",
                typeOfCategory,
                typeOfTransaction);

        if (!typeOfCategory.equals(typeOfTransaction)) {
            log.error(
                    "[checkNewTypeOfTransactionWithTypeOfCategory] typeOfCategory not equal typeOfTransaction");
            throw new ValidationException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }

    Transaction getTransactionById(UUID id) {
        log.info("[getTransactionById] id={}", id);

        return transactionRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    TransactionResponse toTransactionResponse(Transaction transaction) {
        log.info("[toTransactionResponse]");

        TransactionResponse response =
                TransactionMapper.INSTANCE.toTransactionResponse(transaction);
        response.setNote(transaction.getNote());
        return response;
    }
}
