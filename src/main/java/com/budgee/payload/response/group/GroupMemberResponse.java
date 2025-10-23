package com.budgee.payload.response.group;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupMemberResponse {

    UUID memberId;
    String memberName;
    BigDecimal totalSponsorship;
    BigDecimal totalAdvanceAmount;
    Boolean isCreator;
}
