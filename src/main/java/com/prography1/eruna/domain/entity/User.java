package com.prography1.eruna.domain.entity;

import com.prography1.eruna.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User extends BaseTimeEntity{
    @Column(name="user_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String uuid;

    @Column(unique = true)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    public static User join(String uuid, String fcmToken){
        return User.builder().uuid(uuid).fcmToken(fcmToken).role(Role.USER).build();
    }
}
