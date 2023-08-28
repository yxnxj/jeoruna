package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.entity.Wakeup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WakeupRepository extends JpaRepository<Wakeup, Long> {

    Optional<Wakeup> findByUser(User user);
}
