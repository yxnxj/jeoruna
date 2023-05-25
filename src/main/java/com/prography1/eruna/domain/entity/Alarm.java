package com.prography1.eruna.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Alarm extends BaseTimeEntity{

    @Column(name="alarm_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="group_id")
    private Groups groups;

    @Temporal(TemporalType.DATE)
    private LocalDate startDate;

    @Temporal(TemporalType.DATE)
    private LocalDate finishDate;

    private String alarmSound;

    @Temporal(TemporalType.TIME)
    private LocalTime alarmTime;

    private Boolean alarmRepeat;

    @Builder
    public Alarm(Groups groups, LocalDate startDate, LocalDate finishDate, String alarmSound,
                 LocalTime alarmTime) {
        this.groups = groups;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.alarmSound = alarmSound;
        this.alarmTime = alarmTime;
        this.alarmRepeat = true;
    }
}
