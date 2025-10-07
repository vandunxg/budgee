package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.SubscriptionDuration;

@Getter
@Setter
@Entity
@Table(name = "subscription_plans")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class SubscriptionPlan extends BaseEntity {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    String name;

    @NotNull(message = "Duration is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SubscriptionDuration duration;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter code")
    @Column(length = 3)
    String currency = "VND";

    @Size(max = 1000, message = "Features must be at most 1000 characters")
    String features;

    @Size(max = 500, message = "Description must be at most 500 characters")
    String description;
}
