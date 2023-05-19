package com.prography1.eruna.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class GroupResDto {

    private GroupResDto(){}

    @Schema(title = "Group 생성 시 GroupId 반환")
    @Getter
    @AllArgsConstructor
    public static class CreatedGroup {
        @Schema(description = "GroupId", example = "1")
        private Long groupId;
    }
}
