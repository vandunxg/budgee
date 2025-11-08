package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.enums.TransactionType;

@Getter
@Setter
@Entity
@Table(name = "group_transactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"member", "group"})
@EqualsAndHashCode(callSuper = true)
public class GroupTransaction extends BaseEntity {

    @NotNull(message = "Member is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    GroupMember member;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TransactionType type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date must be in the past or present")
    @Column(nullable = false)
    LocalDate date;

    @NotNull(message = "Time is required")
    @Column(nullable = false)
    LocalTime time;

    @Size(max = 1000, message = "Note must be at most 1000 characters")
    String note;

    @Size(max = 255, message = "Image URL must be at most 255 characters")
    @Pattern(regexp = "^(http|https)://.*$", message = "Invalid image URL format")
    String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    Group group;

    @Enumerated(EnumType.STRING)
    GroupExpenseSource groupExpenseSource;

    public boolean isExpenseType() {
        return TransactionType.EXPENSE.equals(this.type);
    }

    public boolean isIncomeType() {
        return TransactionType.EXPENSE.equals(this.type);
    }

    public boolean isContributeType() {
        return TransactionType.EXPENSE.equals(this.type);
    }

    public boolean isMemberAdvanceSource() {
        return GroupExpenseSource.MEMBER_ADVANCE.equals(this.groupExpenseSource);
    }

    public boolean isMemberSponsorSource() {
        return GroupExpenseSource.MEMBER_SPONSOR.equals(this.groupExpenseSource);
    }

    public boolean isGroupFundSource() {
        return GroupExpenseSource.GROUP_FUND.equals(this.groupExpenseSource);
    }

    public boolean isTransactionOwner(GroupMember member) {

        return Objects.equals(member.getId(), this.member.getId());
    }
}
