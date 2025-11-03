package com.budgee.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import com.budgee.enums.WalletType;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletResponse implements Serializable {

    UUID walletId;
    String name;
    BigDecimal balance;
    WalletType type;
    String currency;
    String description;
    Boolean isDefault;
    Boolean isTotalIgnored;
}
