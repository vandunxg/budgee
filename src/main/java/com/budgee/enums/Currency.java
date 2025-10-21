package com.budgee.enums;

import lombok.Getter;

@Getter
public enum Currency {
    VND("VND"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    JPY("JPY");

    private final String code;

    Currency(String code) {
        this.code = code;
    }
}
