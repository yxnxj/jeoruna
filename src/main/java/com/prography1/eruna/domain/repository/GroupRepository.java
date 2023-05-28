package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Groups, Long> {
    Optional<Groups> findByAlarm(Alarm alarm);

    Boolean existsByCode(String code);
    Optional<Groups> findByCode(String code);
}
