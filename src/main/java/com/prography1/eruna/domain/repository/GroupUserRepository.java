package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.GroupUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUser.GroupUserId> {

    Boolean existsByGroupsAndNickname(Groups group, String nickname);

}
