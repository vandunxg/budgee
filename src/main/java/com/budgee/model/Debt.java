package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.DebtStatus;

@Getter
@Setter
@Entity
@Table(name = "debts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Debt extends BaseEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotBlank(message = "Counterparty is required")
    @Size(max = 255, message = "Counterparty must be at most 255 characters")
    @Column(nullable = false)
    String counterparty;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @FutureOrPresent(message = "Due date must be in the present or future")
    LocalDate dueDate;

    boolean isOwedToMe = true;

    @Enumerated(EnumType.STRING)
    DebtStatus status = DebtStatus.PENDING;

    @Size(max = 1000, message = "Note must be at most 1000 characters")
    String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    Group group;
}
