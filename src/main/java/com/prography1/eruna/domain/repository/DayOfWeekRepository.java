package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, DayOfWeek.DayOfWeekId> {
    List<DayOfWeek> findAllByAlarm(Alarm alarm);
}
