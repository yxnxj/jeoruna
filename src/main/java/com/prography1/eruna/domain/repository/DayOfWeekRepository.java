package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.enums.Week;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, DayOfWeek.DayOfWeekId> {
    List<DayOfWeek> findAllByAlarm(Alarm alarm);
    @Query("SELECT dayOfWeek.alarm From DayOfWeek dayOfWeek WHERE dayOfWeek.dayOfWeekId.day = :today ")
    List<Alarm> findAllAlarmsByDay(Week today);
}
