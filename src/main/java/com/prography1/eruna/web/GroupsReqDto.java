package com.prography1.eruna.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GroupsReqDto {

    private GroupsReqDto(){};

    @Schema(title = "유저 그룹 참가")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupJoinUserInfo {
        @Schema(description = "그룹에 참여하는 유저 정보",
                example = "{\n" +
                "\t\"uuid\": \"1111-AAAA-UUID-1234\",\n" +
                "\t\"nickname\" : \"닉네임1\",\n" +
                "\t\"phoneNum\" : \"01012345678\"\n" +
                "}")
        private String uuid;
        private String nickname;
        private String phoneNum;
    }
}
