package com.budgee.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.budgee.enums.TransactionType;

@Getter
@Setter
@Entity
@Table(name = "categories")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity implements OwnerEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    String name;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TransactionType type;

    @Size(max = 500, message = "Description must be at most 500 characters")
    String description;

    @Builder.Default Boolean isDefault = false;

    @Size(max = 255, message = "Color must be at most 255 characters")
    @Column(length = 255)
    String color;

    @Size(max = 255, message = "Icon must be at most 255 characters")
    @Column(length = 255)
    String icon;

    @Override
    public User getOwner() {
        return this.user;
    }
}
