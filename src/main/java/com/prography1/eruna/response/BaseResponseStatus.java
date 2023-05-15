package com.prography1.eruna.response;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {

    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),

    /**
     * 2000 : Request 오류
     */
    VALIDATION_ERROR(false, 2000, "입력값을 확인해주세요."),
    USER_NOT_FOUND(false, 2001, "해당 유저를 찾을 수 없습니다."),
    INVALID_UUID_TOKEN(false,2002,"유효하지 않은 UUID 토큰입니다."),
    INVALID_GROUP_CODE(false,2003,"유효하지 않은 그룹 코드입니다."),
    INVALID_FCM_TOKEN(false,2004,"유효하지 않은 FCM 토큰입니다."),
    DUPLICATED_NICKNAME(false,2005,"이미 존재하는 닉네임입니다."),
    NOT_HOST(false,2006,"host 권한이 필요합니다."),

    /**
     * 3000 : Response 오류
     */
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    ;

    private final boolean isSuccess;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}