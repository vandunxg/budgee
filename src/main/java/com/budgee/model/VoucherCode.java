package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.VoucherDiscountType;

@Getter
@Setter
@Entity
@Table(name = "voucher_codes")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class VoucherCode extends BaseEntity {

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must be at most 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    String code;

    @NotNull(message = "Discount type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VoucherDiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.00", message = "Discount value must be non-negative")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal discountValue;

    @Min(value = 1, message = "Max uses must be at least 1")
    int maxUses = 1;

    @Min(value = 0, message = "Uses count must be non-negative")
    int usesCount = 0;

    @NotNull(message = "Valid from is required")
    @Column(nullable = false)
    LocalDate validFrom;

    @NotNull(message = "Valid to is required")
    @FutureOrPresent(message = "Valid to must be in the present or future")
    @Column(nullable = false)
    LocalDate validTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    SubscriptionPlan plan;
}
