package com.budgee.service.impl.group;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.enums.TransactionType;
import com.budgee.model.Group;
import com.budgee.model.GroupTransaction;
import com.budgee.payload.response.group.GroupSummary;
import com.budgee.repository.GroupTransactionRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-SUMMARY-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupSummaryService {

    // -------------------------------------------------------------------
    // DEPENDENCIES
    // -------------------------------------------------------------------
    GroupTransactionRepository groupTransactionRepository;

    // -------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------

    public GroupSummary calculateGroupSummary(Group group) {
        log.info("[calculateGroupSummary] groupId={}", group.getId());

        List<GroupTransaction> transactions = groupTransactionRepository.findAllByGroup(group);

        BigDecimal totalIncome = calculateTotalIncome(transactions);
        BigDecimal totalExpense = calculateTotalExpense(transactions);
        BigDecimal totalSponsorship = calculateTotalSponsorship(transactions);

        BigDecimal balance =
                totalIncome.add(totalSponsorship).subtract(totalExpense).max(BigDecimal.ZERO);

        return GroupSummary.builder()
                .balance(balance)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalSponsorship(totalSponsorship)
                .build();
    }

    public BigDecimal calculateNetExpense(Group group) {
        log.info("[calculateNetExpense] groupId={}", group.getId());

        List<GroupTransaction> transactions = groupTransactionRepository.findAllByGroup(group);

        BigDecimal expense = calculateTotalExpense(transactions);
        BigDecimal sponsorship = calculateTotalSponsorship(transactions);

        return expense.subtract(sponsorship);
    }

    public BigDecimal calculateAdvancePayment(Group group) {
        log.info("[calculateAdvancePayment] groupId={}", group.getId());

        List<GroupTransaction> transactions = groupTransactionRepository.findAllByGroup(group);

        return calculateAdvancePaymentFromMember(transactions);
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    BigDecimal calculateAdvancePaymentFromMember(List<GroupTransaction> transactions) {
        log.info("[calculateAdvancePaymentFromMember]");

        return transactions.stream()
                .filter(this::isAdvancePaymentFromMember)
                .map(GroupTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal calculateTotalSponsorship(List<GroupTransaction> transactions) {
        log.info("[calculateTotalSponsorship]");

        return transactions.stream()
                .filter(this::isSponsorshipTransaction)
                .map(GroupTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal calculateTotalIncome(List<GroupTransaction> transactions) {
        log.info("[calculateTotalIncome]");

        return transactions.stream()
                .filter(x -> TransactionType.INCOME.equals(x.getType()))
                .map(GroupTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal calculateTotalExpense(List<GroupTransaction> transactions) {
        log.info("[calculateTotalExpense]");

        return transactions.stream()
                .filter(x -> TransactionType.EXPENSE.equals(x.getType()))
                .map(GroupTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    boolean isAdvancePaymentFromMember(GroupTransaction transaction) {
        log.info("[isAdvancePaymentFromMember]");

        return Objects.equals(
                GroupExpenseSource.MEMBER_ADVANCE, transaction.getGroupExpenseSource());
    }

    boolean isSponsorshipTransaction(GroupTransaction tx) {
        log.info("[isSponsorshipTransaction]");

        return (tx.getType() == TransactionType.EXPENSE
                        && tx.getGroupExpenseSource() == GroupExpenseSource.MEMBER_SPONSOR)
                || tx.getType() == TransactionType.CONTRIBUTE;
    }
}
