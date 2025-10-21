package com.budgee.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.budgee.enums.TransactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse implements Serializable {

    UUID transactionId;
    UUID walletId;
    UUID categoryId;
    BigDecimal amount;
    TransactionType type;
    String note;
    LocalDate date;
    LocalTime time;
}
