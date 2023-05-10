package com.prography1.eruna.domain.entity;

import com.prography1.eruna.domain.enums.AlarmSound;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Time;
import java.time.LocalDate;

@AllArgsConstructor
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

    @Enumerated(EnumType.STRING)
    private AlarmSound alarmSound;

    @Temporal(TemporalType.TIME)
    private Time alarmTime;

    @ColumnDefault("'Y'")
    private String alarmRepeat;
}
