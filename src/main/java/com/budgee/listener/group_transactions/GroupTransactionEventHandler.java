package com.budgee.listener.group_transactions;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.budgee.event.application.GroupDeletedEvent;
import com.budgee.event.application.GroupTransactionCreatedEvent;
import com.budgee.repository.GroupTransactionRepository;
import com.budgee.service.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-TRANSACTION-EVENT-HANDLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupTransactionEventHandler {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupTransactionRepository groupTransactionRepository;

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------
    EmailService emailService;

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onGroupDeleted(GroupDeletedEvent event) {
        UUID groupId = event.groupId();
        log.info("[onGroupDeleted] categoryId={}", groupId);

        log.warn("[onGroupDeleted] delete all group transactions by groupId={}", groupId);

        groupTransactionRepository.deleteAllByGroupId(groupId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGroupTransactionCreated(GroupTransactionCreatedEvent event) {
        UUID transactionId = event.transactionId();
        UUID groupId = event.groupId();
        log.info("[onGroupTransactionCreated] groupId={} transactionId={}", groupId, transactionId);

        emailService.sendGroupTransactionCreatedEmail(groupId, transactionId);
    }
}
