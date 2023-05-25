package com.prography1.eruna.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Groups extends BaseTimeEntity{

    @Column(name="group_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String name;

    private String penalty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="host_id")
    private User host;

    @Builder
    public Groups(String code, String name, String penalty, User host) {
        this.code = code;
        this.name = name;
        this.penalty = penalty;
        this.host = host;
    }

    public static Groups create(User host, String penalty){
        String newCode = generateCode();
        return Groups.builder().code(newCode).penalty(penalty).host(host).build();
    }

    private static String generateCode(){

        int codeLength = 6;

        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";

        StringBuilder sb = new StringBuilder(codeLength);

        for (int i = 0; i < codeLength; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }

        return sb.toString();
    }
}
