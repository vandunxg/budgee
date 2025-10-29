package com.budgee.listener.transactions;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.budgee.event.application.CategoryDeletedEvent;
import com.budgee.event.application.WalletDeletedEvent;
import com.budgee.repository.TransactionRepository;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION-EVENT-HANDLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionEventHandler {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    TransactionRepository transactionRepository;

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onCategoryDeleted(CategoryDeletedEvent event) {
        UUID categoryId = event.categoryId();
        UUID ownerId = event.ownerId();

        log.info("[onCategoryDeleted] categoryId={} owner={}", categoryId, ownerId);

        log.warn(
                "[onCategoryDeleted] delete all transaction by categoryId={} ownerId={}",
                categoryId,
                ownerId);
        transactionRepository.deleteAllByCategoryIdAndUserId(categoryId, ownerId);
    }

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onWalletDeleted(WalletDeletedEvent event) {
        UUID walletId = event.walletId();
        UUID ownerId = event.ownerId();

        log.info("[onWalletDeleted] walletId={} owner={}", walletId, ownerId);

        log.warn(
                "[onWalletDeleted] delete all transaction by walletId={} ownerId={}",
                walletId,
                ownerId);
        transactionRepository.deleteAllByWalletIdAndUserId(walletId, ownerId);
    }
}
