package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.enums.Week;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    Optional<Alarm> findByGroups(Groups group);
    @Query(value = "\"SELECT alarm FROM Alarm alarm WHERE \" +\n" +
            "                \"EXISTS (SELECT d FROM alarm.weekList d where d.dayOfWeekId.day = :today)\"", nativeQuery = true)
    List<Alarm> findAllOnDay(@Param("today") String today);

    List<Alarm> findByWeekList_DayOfWeekId_Day(Week day);

}
