package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.budgee.enums.GroupRole;

@Getter
@Setter
@Entity
@Table(
        name = "group_members",
        indexes = {@Index(columnList = "group_id"), @Index(columnList = "user_id")})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"group", "user"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class GroupMember extends BaseEntity {

    @NotNull(message = "Group is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @NotNull(message = "Member name is required")
    @Column(nullable = false)
    String memberName;

    @NotNull(message = "Joined at is required")
    @PastOrPresent(message = "Joined at must be in the past or present")
    @Column(nullable = false)
    LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    GroupRole role;

    @NotNull(message = "Balance owed is required")
    @DecimalMin(value = "0.00", message = "Balance owed must be non-negative")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal balanceOwed;

    @NotNull(message = "Advance amount is required")
    @DecimalMin(value = "0.00", message = "Advance amount must be non-negative")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal advanceAmount;
}
