package com.budgee.payload.response.group;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupResponse {

    UUID groupId;
    String groupName;
    BigDecimal totalSponsorship;
    BigDecimal totalIncome;
    BigDecimal totalExpense;
    BigDecimal totalIncomeAndSponsorship;
    BigDecimal totalRemaining;
    LocalDate startDate;
    List<GroupMemberResponse> members;
}
