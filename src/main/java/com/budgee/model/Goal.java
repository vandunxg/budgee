package com.budgee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "goals")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Goal extends BaseEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @NotNull(message = "Wallet is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    Wallet wallet;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be at least 0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal targetAmount;

    @NotNull(message = "Current amount is required")
    @DecimalMin(value = "0.00", message = "Current amount must be non-negative")
    @Column(precision = 15, scale = 2)
    BigDecimal currentAmount = BigDecimal.ZERO;

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    LocalDate startDate;

    @FutureOrPresent(message = "End date must be in the present or future")
    LocalDate endDate;
}