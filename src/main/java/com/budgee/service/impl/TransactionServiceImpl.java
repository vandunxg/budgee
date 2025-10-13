package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

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

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    TransactionRepository transactionRepository;
    WalletService walletService;
    CategoryService categoryService;
    UserService userService;

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("[createTransaction]={}", request);

        Transaction transaction = TransactionMapper.INSTANCE.toTransaction(request);

        Wallet wallet = walletService.getWalletByIdForOwner(request.walletId());
        transaction.setWallet(wallet);
        wallet.decrease(request.amount());

        Category category = categoryService.getCategoryByIdForOwner(request.categoryId());
        transaction.setCategory(category);

        User user = userService.getCurrentUser();
        transaction.setUser(user);

        log.warn("[createTransaction] save to db");
        transactionRepository.save(transaction);

        return toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse updateTransaction(UUID id, TransactionRequest request) {
        log.info("[updateTransaction]={}", request);

        Transaction transaction = getTransactionById(id);

        Wallet walletOfTransaction = transaction.getWallet();
        Wallet walletFromRequest = walletService.getWalletByIdForOwner(request.walletId());

        BigDecimal currentAmount = transaction.getAmount();

        Category category = categoryService.getCategoryByIdForOwner(request.categoryId());

        checkNewTypeOfTransactionWithTypeOfCategory(category.getType(), request.type());

        if (!walletOfTransaction.getId().equals(walletFromRequest.getId())) {
            transaction.setWallet(walletFromRequest);
        }

        checkUpdateTransaction(
                walletOfTransaction,
                walletFromRequest,
                currentAmount,
                request.amount(),
                transaction.getType(),
                request.type());

        transaction.setAmount(request.amount());

        transaction.setCategory(category);

        transaction.setDate(request.date());
        transaction.setTime(request.time());
        transaction.setType(request.type());
        transaction.setNote(request.note());

        log.warn("[updateTransaction] update to db");
        transactionRepository.save(transaction);

        return toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse getTransaction(UUID id) {
        log.info("[getTransaction] id={}", id);

        Transaction transaction = getTransactionById(id);

        return toTransactionResponse(transaction);
    }

    @Override
    public void deleteTransaction(UUID id) {
        log.info("[deleteTransaction] id={}", id);

        Transaction transaction = getTransactionById(id);

        log.warn("[deleteTransaction] delete from db");
        transactionRepository.delete(transaction);
    }

    // PRIVATE FUNCTION

    //    if the type of the current category not equals a request type throw error
    void checkNewTypeOfTransactionWithTypeOfCategory(
            TransactionType typeOfCategory, TransactionType typeOfTransaction) {
        log.info(
                "[checkNewTypeOfTransactionWithTypeOfCategory] typeOfCategory={} typeOfTransaction={}",
                typeOfCategory,
                typeOfTransaction);

        if (!typeOfCategory.equals(typeOfTransaction)) {
            throw new ValidationException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }

    void checkUpdateTransaction(
            Wallet currentWallet,
            Wallet newWallet,
            BigDecimal currentAmount,
            BigDecimal newAmount,
            TransactionType currentType,
            TransactionType newType) {
        log.info("[checkUpdateTransaction]");

        if (currentWallet.getId().equals(newWallet.getId())
                && currentAmount.compareTo(newAmount) == 0
                && newType.equals(currentType)) {
            log.info("[checkUpdateTransaction] skip change");

            return;
        }

        walletService.updateWalletBalance(
                currentWallet, newWallet, currentAmount, newAmount, currentType, newType);
    }

    Transaction getTransactionById(UUID id) {
        log.info("[getTransactionById]={}", id);

        return transactionRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    TransactionResponse toTransactionResponse(Transaction transaction) {
        log.info("[toTransactionResponse]={}", transaction);

        TransactionResponse response =
                TransactionMapper.INSTANCE.toTransactionResponse(transaction);
        response.setNote(transaction.getNote());

        return response;
    }
}
