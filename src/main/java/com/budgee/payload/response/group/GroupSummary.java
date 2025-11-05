package com.budgee.payload.response.group;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupSummary implements Serializable {

    BigDecimal balance;
    BigDecimal totalIncome;
    BigDecimal totalExpense;
    BigDecimal totalSponsorship;
}
