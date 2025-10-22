package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.enums.TransactionSource;
import com.budgee.enums.TransactionType;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.exception.ValidationException;
import com.budgee.mapper.TransactionMapper;
import com.budgee.model.*;
import com.budgee.payload.request.TransactionRequest;
import com.budgee.payload.request.group.GroupTransactionRequest;
import com.budgee.payload.response.TransactionResponse;
import com.budgee.payload.response.group.GroupTransactionResponse;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.TransactionRepository;
import com.budgee.service.CategoryService;
import com.budgee.service.TransactionService;
import com.budgee.service.UserService;
import com.budgee.service.WalletService;
import com.budgee.util.SecurityHelper;
import com.budgee.util.WalletHelper;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    GroupMemberRepository groupMemberRepository;
    TransactionRepository transactionRepository;

    WalletService walletService;
    CategoryService categoryService;
    UserService userService;

    SecurityHelper securityHelper;
    WalletHelper walletHelper;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("[createTransaction] request={}", request);

        Transaction transaction = TransactionMapper.INSTANCE.toTransaction(request);

        Wallet wallet = walletHelper.getWalletByIdForOwner(request.walletId());
        Category category = categoryService.getCategoryByIdForOwner(request.categoryId());
        User user = userService.getCurrentUser();

        checkNewTypeOfTransactionWithTypeOfCategory(category.getType(), request.type());

        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setUser(user);
        transaction.setTransactionSource(TransactionSource.PERSONAL);

        walletService.applyTransaction(wallet, transaction);

        log.debug("[createTransaction] saving transaction...");
        transactionRepository.save(transaction);

        return toTransactionResponse(transaction);
    }

    @Override
    public GroupTransactionResponse createGroupTransaction(
            GroupTransactionRequest request, UUID groupId) {
        log.info("[createGroupTransaction]={}", request);

        Transaction transaction = TransactionMapper.INSTANCE.toTransaction(request);
        GroupMember member = getMemberById(request.memberId());

        switch (request.groupExpenseSource()) {
            case GROUP_FUND -> transaction.setGroupExpenseSource(GroupExpenseSource.GROUP_FUND);
            case MEMBER_ADVANCE -> transaction.setGroupExpenseSource(
                    GroupExpenseSource.MEMBER_ADVANCE);
            case MEMBER_SPONSOR -> transaction.setGroupExpenseSource(
                    GroupExpenseSource.MEMBER_SPONSOR);
            default -> throw new ValidationException(ErrorCode.INVALID_GROUP_TRANSACTION_SOURCE);
        }

        if (!Objects.isNull(member.getUser())) {
            transaction.setUser(member.getUser());
        }

        transaction.setGroupMember(member);
        transaction.setTransactionSource(TransactionSource.GROUP);

        return null;
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

        return toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse getTransaction(UUID id) {
        log.info("[getTransaction] id={}", id);

        Transaction transaction = this.getTransactionById(id);
        securityHelper.checkIsOwner(transaction);
        return toTransactionResponse(transaction);
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

    // -----------------------------------------------------
    // UTILITIES

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

    GroupMember getMemberById(UUID id) {
        log.info("[getMemberById]={}", id);

        return groupMemberRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
    }

    TransactionResponse toTransactionResponse(Transaction transaction) {
        log.info("[toTransactionResponse]");

        TransactionResponse response =
                TransactionMapper.INSTANCE.toTransactionResponse(transaction);
        response.setNote(transaction.getNote());
        return response;
    }
}
