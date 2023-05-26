package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Groups, Long> {
}
