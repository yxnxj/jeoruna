package com.prography1.eruna.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class UserResDto {

    private UserResDto(){}

    @Schema(title = "UUID 토큰 발급")
    @Getter
    @AllArgsConstructor
    public static class UUID {
        @Schema(description = "UUID 토큰", example = "6e383010-7621-437b-98d5-fe2147465ac0")
        private String uuidToken;
    }
}
