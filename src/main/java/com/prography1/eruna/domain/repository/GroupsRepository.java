package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupsRepository extends JpaRepository<Groups, Long> {
    Boolean existsByCode(String code);
    Optional<Groups> findByCode(String code);
}
