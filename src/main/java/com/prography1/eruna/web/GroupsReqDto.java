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
        @Schema(description = "그룹에 참여하는 유저 uuid", example = "1111-AAAA-UUID-1234")
        private String uuid;
        @Schema(description = "새 유저 닉네임", example = "닉네임1")
        private String nickname;
        @Schema(description = "유저 전화번호", example ="01012345678")
        private String phoneNum;
    }
}
