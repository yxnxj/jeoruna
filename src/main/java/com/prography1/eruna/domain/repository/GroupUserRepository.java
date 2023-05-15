package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupUserRepository extends JpaRepository<GroupUser, Long> {
    Boolean existsByGroupsAndNickname(Groups group, String nickname);

}
