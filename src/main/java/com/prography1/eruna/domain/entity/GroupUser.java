package com.prography1.eruna.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class GroupUser extends BaseTimeEntity{
    @EmbeddedId
    private GroupUserId groupUserId;

    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Groups groups;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String nickname;

    private String phoneNum;

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class GroupUserId implements Serializable {
        private Long groupId;
        private Long userId;
    }

    @Builder
    public GroupUser(Groups groups, User user, String nickname, String phoneNum, GroupUserId groupUserId) {
        this.groups = groups;
        this.user = user;
        this.nickname = nickname;
        this.phoneNum = phoneNum;
        this.groupUserId = groupUserId;
    }
}
