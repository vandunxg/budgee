package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.enums.TransactionSource;
import com.budgee.enums.TransactionType;

@Getter
@Setter
@Entity
@Table(name = "transactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "wallet", "category", "recurring", "group", "debt"})
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseEntity implements OwnerEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotNull(message = "Wallet is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    Category category;

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
    @Column(length = 255)
    String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_id")
    RecurringTransaction recurring;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_id")
    GroupMember groupMember;

    @Enumerated(EnumType.STRING)
    TransactionSource transactionSource;

    @Enumerated(EnumType.STRING)
    GroupExpenseSource groupExpenseSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id")
    Debt debt;

    @Override
    public User getOwner() {
        return this.user;
    }
}
