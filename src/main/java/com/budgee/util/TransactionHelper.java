package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.model.Transaction;
import com.budgee.service.TransactionService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION_HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionHelper {

    TransactionService transactionService;

    Transaction getTransactionById(UUID id) {
        log.info("[getTransactionById] id={}", id);

        return transactionService.getTransactionById(id);
    }
}
