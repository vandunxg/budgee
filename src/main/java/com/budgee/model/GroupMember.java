package com.budgee.model;

import com.budgee.enums.GroupRole;
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
@Table(name = "group_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"group_id", "user_id"})
        },
        indexes = {
                @Index(columnList = "group_id"),
                @Index(columnList = "user_id")
        })
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"group", "user"})
@EqualsAndHashCode(callSuper = true)
public class GroupMember extends BaseEntity {

    @NotNull(message = "Group is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotNull(message = "Joined at is required")
    @PastOrPresent(message = "Joined at must be in the past or present")
    @Column(nullable = false)
    LocalDateTime joinedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    GroupRole role = GroupRole.MEMBER;

    @NotNull(message = "Balance owed is required")
    @DecimalMin(value = "0.00", message = "Balance owed must be non-negative")
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal balanceOwed = BigDecimal.ZERO;
}