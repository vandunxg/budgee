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
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.TransactionRepository;
import com.budgee.service.CategoryService;
import com.budgee.service.TransactionService;
import com.budgee.service.UserService;
import com.budgee.service.WalletService;
import com.budgee.util.SecurityHelper;
import com.budgee.util.WalletHelper;

import static com.budgee.enums.TransactionType.EXPENSE;
import static com.budgee.enums.TransactionType.INCOME;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    TransactionRepository transactionRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    WalletService walletService;
    CategoryService categoryService;
    UserService userService;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    TransactionMapper transactionMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    WalletHelper walletHelper;
    SecurityHelper securityHelper;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("[createTransaction] request={}", request);

        Wallet wallet = walletHelper.getWalletByIdForOwner(request.walletId());
        Category category = categoryService.getCategoryByIdForOwner(request.categoryId());
        User user = userService.getCurrentUser();

        Transaction transaction = transactionMapper.toTransaction(request, wallet, category, user);

        checkNewTypeOfTransactionWithTypeOfCategory(category.getType(), request.type());

        setTransactionType(request.type(), transaction);

        walletService.applyTransaction(wallet, transaction);

        log.debug("[createTransaction] saving transaction...");
        transactionRepository.save(transaction);

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(UUID id, TransactionRequest request) {
        log.info("[updateTransaction] id={} request={}", id, request);

        Transaction transaction = getTransactionById(id);
        Category newCategory = categoryService.getCategoryByIdForOwner(request.categoryId());
        Wallet newWallet = walletHelper.getWalletByIdForOwner(request.walletId());
        Wallet oldWallet = transaction.getWallet();

        BigDecimal oldAmount = transaction.getAmount();
        BigDecimal newAmount = request.amount();
        TransactionType oldType = transaction.getType();
        TransactionType newType = request.type();

        securityHelper.checkIsOwner(transaction);
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

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse getTransaction(UUID id) {
        log.info("[getTransaction] id={}", id);

        Transaction transaction = this.getTransactionById(id);
        securityHelper.checkIsOwner(transaction);

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

        Transaction transaction = this.getTransactionById(id);
        securityHelper.checkIsOwner(transaction);

        walletService.reverseTransaction(transaction.getWallet(), transaction);

        transactionRepository.delete(transaction);
        log.warn("[deleteTransaction] deleted transaction id={}", id);
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

    void setTransactionType(TransactionType type, Transaction transaction) {

        switch (type) {
            case INCOME -> transaction.setType(INCOME);
            case EXPENSE -> transaction.setType(EXPENSE);
            default -> throw new ValidationException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }

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
}
