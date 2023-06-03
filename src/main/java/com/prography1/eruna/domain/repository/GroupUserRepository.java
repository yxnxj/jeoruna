package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUser.GroupUserId> {

    Boolean existsByGroupsAndNickname(Groups group, String nickname);

    Optional<GroupUser> findByNickname(String nickname);
}
