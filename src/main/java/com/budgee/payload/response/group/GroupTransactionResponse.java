package com.budgee.payload.response.group;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.enums.TransactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupTransactionResponse implements Serializable {

    UUID transactionId;
    UUID groupId;
    CreatorTransactionResponse creator;
    GroupExpenseSource groupExpenseSource;
    BigDecimal amount;
    TransactionType type;
    String note;
    LocalDate date;
    LocalTime time;
}
