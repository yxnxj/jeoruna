package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUser.GroupUserId> {

    Boolean existsByGroupsAndNickname(Groups group, String nickname);

    @EntityGraph(attributePaths = {"user"})
    List<GroupUser> findByGroups(Groups group);
}
