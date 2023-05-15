package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupsRepository extends JpaRepository<Groups, Long> {
    Boolean existsByCode(String code);
    Groups findByCode(String code);
}
