package com.prography1.eruna.domain.enums;

import lombok.Getter;

@Getter
public enum AlarmSound {

    ALARM_SIU("siuuuuuu.wav"),
    ALARM_DEFAULT("default.wav")
    ;
    private final String filename;
    AlarmSound(String filename) {
        this.filename = filename;
    }
}
