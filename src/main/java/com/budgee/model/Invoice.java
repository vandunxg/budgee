package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.Currency;
import com.budgee.enums.InvoiceStatus;
import com.budgee.enums.PaymentMethod;

@Getter
@Setter
@Entity
@Table(name = "invoices")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Invoice extends BaseEntity {

    @NotNull(message = "Subscription is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    UserSubscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    InvoiceStatus status;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Builder.Default
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter code")
    @Column(length = 3)
    Currency currency = Currency.VND;

    @NotNull(message = "Method is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod method;

    @PastOrPresent(message = "Paid at must be in the past or present")
    LocalDateTime paidAt;

    LocalDateTime issuedAt;
}
