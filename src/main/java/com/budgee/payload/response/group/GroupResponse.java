package com.budgee.payload.response.group;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupResponse {

    UUID groupId;
    String groupName;
    BigDecimal totalSponsorship;
    BigDecimal totalIncome;
    BigDecimal totalExpense;
    BigDecimal totalRemaining;
    LocalDate startDate;
    List<GroupMemberResponse> members;
}
