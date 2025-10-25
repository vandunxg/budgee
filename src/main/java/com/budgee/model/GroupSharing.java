package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.GroupRole;
import com.budgee.enums.GroupSharingStatus;

@Getter
@Setter
@Entity
@Table(name = "group_sharings")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"group", "sharedUser"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class GroupSharing extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_user_id")
    User sharedUser;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    GroupRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    GroupSharingStatus status;

    @Column(name = "invite_token", unique = true, length = 64)
    String sharingToken;

    LocalDateTime joinedAt;
    LocalDateTime acceptedAt;

    // -------------------------------------------------------------------
    // UTILITY METHODS
    // -------------------------------------------------------------------

    public boolean isAccepted() {
        return status == GroupSharingStatus.ACCEPTED;
    }

    public boolean isPending() {
        return status == GroupSharingStatus.PENDING;
    }

    public void markAccepted() {
        this.status = GroupSharingStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void markRevoked() {
        this.status = GroupSharingStatus.REVOKED;
    }
}
