package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.budgee.enums.VerificationType;

@Getter
@Setter
@Entity
@Table(name = "verification_codes")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class VerificationCode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VerificationType type;

    @Column(nullable = false, unique = true)
    String code;

    @Column(nullable = false)
    String target;

    @Column(nullable = false)
    LocalDateTime expiresAt;

    LocalDateTime verifiedAt;

    @Column(nullable = false)
    boolean verified;
}
