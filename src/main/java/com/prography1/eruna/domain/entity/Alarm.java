package com.prography1.eruna.domain.entity;

import com.prography1.eruna.domain.enums.AlarmSound;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Alarm extends BaseTimeEntity{

    @Column(name="alarm_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="group_id")
    private Groups groups;

    @Temporal(TemporalType.DATE)
    private LocalDate startDate;

    @Temporal(TemporalType.DATE)
    private LocalDate finishDate;

    @Enumerated(EnumType.STRING)
    private AlarmSound alarmSound;

    @Temporal(TemporalType.TIME)
    private LocalTime alarmTime;

    private Boolean alarmRepeat;

    @OneToMany(mappedBy="alarm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DayOfWeek> weekList = new ArrayList<>();


    @Builder
    public Alarm(Groups groups, LocalDate startDate, LocalDate finishDate, AlarmSound alarmSound,
                 LocalTime alarmTime) {
        this.groups = groups;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.alarmSound = alarmSound;
        this.alarmTime = alarmTime;
        this.alarmRepeat = true;
    }

    public void update(AlarmSound alarmSound, LocalTime alarmTime){
        this.alarmSound = alarmSound;
        this.alarmTime = alarmTime;
    }
}
