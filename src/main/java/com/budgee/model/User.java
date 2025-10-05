package com.budgee.model;

import com.budgee.enums.SubscriptionTier;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must be at most 255 characters")
    @Column(nullable = false)
    String fullname;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone must be at most 20 characters")
    @Column(unique = true, length = 20)
    String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    String email;

    @Past(message = "Birthday must be in the past")
    LocalDate birthday;

    @NotBlank(message = "Password hash is required")
    @Column(nullable = false)
    String passwordHash;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter code")
    @Column(length = 3)
    String currency = "VND";

    @Enumerated(EnumType.STRING)
    SubscriptionTier subscriptionTier = SubscriptionTier.BASIC;

    @FutureOrPresent(message = "Subscription expiry must be in the present or future")
    LocalDate subscriptionExpiry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GroupMember> groupMemberships = new HashSet<>();

}