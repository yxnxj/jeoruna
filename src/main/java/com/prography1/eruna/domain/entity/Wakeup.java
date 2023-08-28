package com.prography1.eruna.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Builder
public class Wakeup{
    @Column(name="wakeup_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="alarm_id")
    private Alarm alarm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    private Boolean wakeupCheck;

    @Temporal(TemporalType.DATE)
    private LocalDate wakeupDate;

    @Temporal(TemporalType.TIME)
    private LocalTime wakeupTime;

    public void userWakeup(LocalTime wakeupTime){
        this.wakeupTime = wakeupTime;
        this.wakeupCheck = true;
    }

}
