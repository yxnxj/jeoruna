package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUser.GroupUserId> {

    Boolean existsByGroupsAndNickname(Groups group, String nickname);


    Optional<GroupUser> findByNickname(String nickname);

    @Query("select g from GroupUser g where g.groups = :group")
    List<GroupUser> findByGroupsForScheduler(Groups group);

    Optional<GroupUser> findGroupUserByUser(User user);
}
