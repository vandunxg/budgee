package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.WalletType;

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
    @DecimalMin(value = "0.00", message = "Balance must be non-negative")
    @Column(precision = 15, scale = 2)
    BigDecimal balance = BigDecimal.ZERO;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter code")
    @Column(length = 3)
    String currency = "VND";

    @Size(max = 500, message = "Description must be at most 500 characters")
    String description;

    boolean isDefault = false;

    @DecimalMin(value = "0.00", message = "Interest rate must be non-negative")
    @DecimalMax(value = "100.00", message = "Interest rate must be at most 100%")
    @Column(precision = 5, scale = 2)
    BigDecimal interestRate;

    @Override
    public User getOwner() {
        return this.user;
    }
}
