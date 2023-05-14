package com.prography1.eruna.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserReqDto {

    private UserReqDto(){}

    @Schema(title = "Fcm 토큰 전달")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FcmToken {
        @Schema(description = "Fcm 토큰", example = "6e383010-7621-437b-98d5-fe2147465ac0")
        private String fcmToken;
    }
}
