package com.budgee.model;


import com.budgee.enums.PaymentMethod;
import com.budgee.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payments")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotNull(message = "Subscription is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    UserSubscription subscription;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter code")
    @Column(length = 3)
    String currency = "VND";

    @NotNull(message = "Method is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod method;

    @Size(max = 255, message = "Transaction ID must be at most 255 characters")
    @Column(length = 255)
    String transactionId;

    @Enumerated(EnumType.STRING)
    PaymentStatus status = PaymentStatus.PENDING;

    @Size(max = 1000, message = "QR code data must be at most 1000 characters")
    String qrCodeData;

    @PastOrPresent(message = "Paid at must be in the past or present")
    LocalDateTime paidAt;
}