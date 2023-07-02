package com.prography1.eruna.web;

import com.prography1.eruna.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

public class UserResDto {

    private UserResDto(){}

    @Schema(title = "UUID 토큰 발급")
    @Getter
    @AllArgsConstructor
    public static class UUID {
        @Schema(description = "UUID 토큰", example = "6e383010-7621-437b-98d5-fe2147465ac0")
        private String uuid;
    }

    @Schema(title = "유저 기상 정보",
            description = "캐싱된 기상 정보, 알람 FCM이 전송될 때 저장된다. \n " +
                          "SSE로 클라이언트에게 전달되며, 기상 POST API로 요청받으면 갱신되어 다시 전달된다.")
    @Getter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class WakeupDto implements Serializable {
        @Schema(title ="UUID 토큰",  description = "", example = "6e383010-7621-437b-98d5-fe2147465ac0")
        private String uuid;
        @Schema(title = "유저 닉네임", example = "user name")
        private String nickname;
        @Schema(title = "기상 상태" , defaultValue = "false", description = "기상 API가 요청됐을 때 true로 갱신된다.", example = "true")
        private Boolean wakeup;
        @Schema(title = "기상 시간", description = "기상 API로 요청 받으면 요청 시간으로 갱신된다.", example = "15:19:47.459", defaultValue = "0:00:00")
        private String wakeupTime;
        @Schema(title = "전화 번호", description = "유저의 전화번호", example = "01012345678")
        private String phoneNum;

//        @Builder
//        public WakeupDto(String uuid, String nickname){
//            this.uuid = uuid;
//            this.nickname = nickname;
//        }


        public static WakeupDto fromUser(User user, String nickname, String phoneNum){
            return WakeupDto.builder()
                    .uuid(user.getUuid())
                    .nickname(nickname)
                    .phoneNum(phoneNum)
                    .wakeup(false)
                    .wakeupTime(LocalTime.of(23,59).toString())
                    .build();
        }
    }

    @Schema(title = "유저가 속한 그룹의 id")
    @Getter
    @AllArgsConstructor
    public static class GroupId {
        @Schema(description = "유저가 속한 그룹의 id", example = "12")
        private Long groupId;
    }
}
