package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUser.GroupUserId> {

    Boolean existsByGroupsAndNickname(Groups group, String nickname);

    Optional<GroupUser> findByNickname(String nickname);

    @Query("select g from GroupUser g JOIN FETCH g.user where g.groups = :group")
    List<GroupUser> findByGroupsForScheduler(@Param("group")Groups group);

    Optional<GroupUser> findGroupUserByUser(User user);

    Optional<GroupUser> findByUser(User user);

    boolean existsByGroupsAndUser(Groups group, User user);

    Long countByGroups(Groups groups);

    boolean existsByUser(User user);
}
