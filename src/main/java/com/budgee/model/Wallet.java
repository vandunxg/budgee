package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.Currency;
import com.budgee.enums.WalletType;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;

@Getter
@Setter
@Entity
@Table(name = "wallets")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Wallet extends BaseEntity implements OwnerEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    String name;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    WalletType type;

    @NotNull(message = "Balance is required")
    @Column(precision = 15, scale = 2)
    BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    Currency currency;

    @Size(max = 500, message = "Description must be at most 500 characters")
    String description;

    @Builder.Default Boolean isDefault = Boolean.FALSE;

    @Builder.Default Boolean isTotalIgnored = Boolean.FALSE;

    @Version private Long version;

    @Override
    public User getOwner() {
        return this.user;
    }

    public void increase(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException(ErrorCode.AMOUNT_MUST_BE_POSITIVE);
        this.balance = this.balance.add(amount);
    }

    public void decrease(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException(ErrorCode.AMOUNT_MUST_BE_POSITIVE);
        this.balance = this.balance.subtract(amount);
    }

    public void transferTo(Wallet target, BigDecimal amount) {
        if (target == null) throw new IllegalArgumentException("Target wallet required");
        this.decrease(amount);
        target.increase(amount);
    }
}
