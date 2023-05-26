package com.prography1.eruna.web;

import com.prography1.eruna.domain.enums.Week;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class GroupReqDto {

    private GroupReqDto() {}

    @Schema(title = "그룹 만들기")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGroup {
        @Schema(description = "UUID 토큰", example = "6e383010-7621-437b-98d5-fe2147465ac0")
        private String uuid;

        @Schema(description = "닉네임", example = "피치푸치")
        private String nickname;

        @Schema(description = "전화번호", example = "01012345678")
        private String phoneNum;

        @Schema(description = "알람 설정", example = "")
        private AlarmInfo alarmInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmInfo{

        @Schema(description = "알람 sound 명", example = "sound_track_1")
        private String sound;

        @Schema(description = "알람 시", example = "13")
        private Integer hours;

        @Schema(description = "알람 분", example = "30")
        private Integer minutes;

        @Schema(description = "반복 요일", example = "[\"MON\", \"SUN\", \"WED\"]")
        private List<Week> week;

        @Schema(description = "벌칙", example = "커피 쏘기!")
        private String penalty;
    }
}
