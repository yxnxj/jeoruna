package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUuid(String uuid);

    Optional<User> findByUuid(String uuid);
}
