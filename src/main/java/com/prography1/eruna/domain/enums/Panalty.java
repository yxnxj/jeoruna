package com.prography1.eruna.domain.enums;

import lombok.Getter;

@Getter
public enum Panalty {
    COFFEE("커피쏘기"),
    WISH("소원들어주기"),
    MONEY("벌금");

    private final String detail;

    Panalty(String detail) {
        this.detail = detail;
    }
}
