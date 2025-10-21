package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "goals")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(callSuper = true)
public class Goal extends BaseEntity implements OwnerEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    String name;

    @NotNull(message = "Categories is required")
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    List<GoalCategory> goalCategories = new ArrayList<>();

    @NotNull(message = "Wallet is required")
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    List<GoalWallet> goalWallets = new ArrayList<>();

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be at least 0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal targetAmount;

    @NotNull(message = "Current amount is required")
    @Column(precision = 15, scale = 2)
    BigDecimal currentAmount = BigDecimal.ZERO;

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    LocalDate startDate;

    @FutureOrPresent(message = "End date must be in the present or future")
    LocalDate endDate;

    @Override
    public User getOwner() {
        return this.user;
    }
}
