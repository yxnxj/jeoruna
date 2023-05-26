package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, DayOfWeek.DayOfWeekId> {
}
