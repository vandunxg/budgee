package com.budgee.service.lookup;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.model.GroupTransaction;
import com.budgee.repository.GroupTransactionRepository;

@Component
@Slf4j(topic = "GROUP-TRANSACTION-LOOKUP")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupTransactionLookup {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupTransactionRepository groupTransactionRepository;

    public GroupTransaction getGroupTransactionById(UUID transactionId) {
        log.info("[getGroupTransactionById] transactionId={}", transactionId);

        return groupTransactionRepository
                .findByIdWithMemberAndUser(transactionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND));
    }
}
