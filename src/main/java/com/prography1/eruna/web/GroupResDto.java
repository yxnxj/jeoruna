package com.prography1.eruna.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class GroupResDto {

    private GroupResDto(){}

    @Schema(title = "Group 생성 시 GroupId 반환")
    @Getter
    @AllArgsConstructor
    public static class CreatedGroup {
        @Schema(description = "GroupId", example = "1")
        private Long groupId;
    }

    @Schema(title = "닉네임 중복 체크")
    @Getter
    @AllArgsConstructor
    public static class IsValidNickname {
        @Schema(description = "nickname 중복 여부 boolean 값, true를 반환하면 사용 가능한 닉네임이다", example = "true")
        private Boolean isValid;

    }

    @Schema(title = "패널티 목록")
    @Getter
    @AllArgsConstructor
    public static class PenaltyList {
        @Schema(description = "패널티 목록", example = "penalty-list : [ \"커피쏘기\", \"소원 들어주기\" ]")
        private List<String> penaltyList;

    }
}
