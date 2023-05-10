package com.prography1.eruna.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
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

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime date;

    private Boolean wakeupCheck;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime wakeupTime;

}
