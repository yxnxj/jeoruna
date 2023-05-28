package com.prography1.eruna.domain.entity;

import com.prography1.eruna.domain.enums.Week;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class DayOfWeek extends BaseTimeEntity{
    @EmbeddedId
    private DayOfWeekId dayOfWeekId;

    @MapsId("alarmId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="alarm_id")
    private Alarm alarm;

    @Embeddable
    @NoArgsConstructor
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class DayOfWeekId implements Serializable {
        private Long alarmId;

        @Enumerated(EnumType.STRING)
        private Week day;
    }

}
