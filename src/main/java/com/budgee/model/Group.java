package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.exception.BusinessException;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;

@Getter
@Setter
@Entity
@Table(name = "`groups`")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"creator", "members"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Group extends BaseEntity implements OwnerEntity {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    String name;

    @NotNull(message = "Creator is required")
    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    User creator;

    @Builder.Default
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance must be non-negative")
    @Column(precision = 15, scale = 2)
    BigDecimal balance = BigDecimal.ZERO;

    int memberCount = 1;

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    LocalDate startDate;

    LocalDate endDate;

    @Builder.Default Boolean isSharing = Boolean.FALSE;

    @Column(unique = true, length = 5)
    String sharingToken;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<GroupMember> members = new HashSet<>();

    @Override
    public User getOwner() {
        return this.creator;
    }

    public void ensureCurrentUserIsMember(User user) {
        boolean isMember =
                members.stream().anyMatch(member -> member.getUser().getId().equals(user.getId()));

        if (!isMember) throw new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
    }

    public void increase(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException(ErrorCode.AMOUNT_MUST_BE_POSITIVE);
        this.balance = this.balance.add(amount);
    }

    public void decrease(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException(ErrorCode.AMOUNT_MUST_BE_POSITIVE);
        this.balance = this.balance.subtract(amount);
    }

    public void ensureSharingEnabled() {
        if (!Boolean.TRUE.equals(isSharing))
            throw new BusinessException(ErrorCode.GROUP_NOT_SHARING);
    }

    public void validateToken(String token) {
        if (!Objects.equals(this.sharingToken, token))
            throw new ValidationException(ErrorCode.SHARING_TOKEN_INVALID);
    }

    public void ensureCreator(User user) {
        if (!Objects.equals(this.creator.getId(), user.getId()))
            throw new ValidationException(ErrorCode.NOT_GROUP_ADMIN);
    }

    public void ensureNotCreator(User user) {
        if (Objects.equals(this.creator.getId(), user.getId()))
            throw new BusinessException(ErrorCode.GROUP_ADMIN_CANT_JOIN);
    }
}
