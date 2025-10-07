package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "`groups`")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"creator", "members", "transactions"})
@EqualsAndHashCode(callSuper = true)
public class Group extends BaseEntity {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    String name;

    @NotNull(message = "Creator is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    User creator;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance must be non-negative")
    @Column(precision = 15, scale = 2)
    BigDecimal balance = BigDecimal.ZERO;

    int memberCount = 1;

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    LocalDate startDate;

    LocalDate endDate;

    @Column(unique = true, length = 255)
    String inviteLink;

    @Column(unique = true, length = 50)
    String inviteId;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GroupMember> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Transaction> transactions = new HashSet<>();
}
