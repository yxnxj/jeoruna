package com.prography1.eruna.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class GroupResDto {
    public GroupResDto() {
    }

    @Schema(title = "닉네임 중복 체크")
    @Getter
    @AllArgsConstructor
    public static class IsValidNickname {
        @Schema(description = "nickname 중복 여부 boolean 값, true를 반환하면 사용 가능한 닉네임이다", example = "true")
        private Boolean isValid;
    }
}
