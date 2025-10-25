package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.model.Transaction;
import com.budgee.repository.TransactionRepository;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION_HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionHelper {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    TransactionRepository transactionRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    public Transaction getTransactionById(UUID id) {
        log.info("[getTransactionById] id={}", id);

        return transactionRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------
}
