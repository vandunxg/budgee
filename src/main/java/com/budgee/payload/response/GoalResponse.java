package com.budgee.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoalResponse {

    UUID goalId;
    String name;
    BigDecimal currentAmount;
    BigDecimal targetAmount;
    List<UUID> categoriesId;
    List<UUID> walletsId;
    LocalDate startDate;
    LocalDate endDate;
}
