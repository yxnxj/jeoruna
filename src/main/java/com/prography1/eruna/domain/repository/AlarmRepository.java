package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    Optional<Alarm> findByGroups(Groups group);
}
