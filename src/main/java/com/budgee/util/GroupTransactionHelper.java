package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.enums.TransactionType;
import com.budgee.model.GroupTransaction;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP_HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupTransactionHelper {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------

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
    public BigDecimal calculateTotalSponsorship(List<GroupTransaction> transactions) {
        log.info("[calculateTotalSponsorship]");

        return transactions.stream()
                .filter(this::isSponsorshipTransaction)
                .map(GroupTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalIncome(List<GroupTransaction> transactions) {
        log.info("[calculateTotalIncome]");

        return transactions.stream()
                .filter(x -> TransactionType.INCOME.equals(x.getType()))
                .map(GroupTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalExpense(List<GroupTransaction> transactions) {
        log.info("[calculateTotalExpense]");

        return transactions.stream()
                .filter(x -> TransactionType.EXPENSE.equals(x.getType()))
                .map(GroupTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    private boolean isSponsorshipTransaction(GroupTransaction tx) {
        return (tx.getType() == TransactionType.EXPENSE
                        && tx.getGroupExpenseSource() == GroupExpenseSource.MEMBER_SPONSOR)
                || tx.getType() == TransactionType.CONTRIBUTE;
    }
}
