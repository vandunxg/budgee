package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.budgee.enums.Currency;
import com.budgee.enums.PaymentGateway;
import com.budgee.enums.PaymentStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_attempts")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentGateway gateway;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    Currency currency;

    String gatewayTransactionId;

    @Column(length = 500)
    String failureReason;

    LocalDateTime processedAt;

    boolean autoRetry;
}
