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
    NOT_FOUND_GROUP(false,2007,"해당 그룹을 찾을 수 없습니다."),
    NOT_FOUND_GROUP_USER(false,2008,"해당 그룹멤버를 찾을 수 없습니다."),
    NOT_FOUND_ALARM(false, 2009, "해당 알람 정보를 찾을 수 없습니다."),
    ALREADY_IN_GROUP_USER(false, 2010, "이미 그룹 내에 존재하는 유저입니다."),
    HOST_CANNOT_EXIT(false, 2011, "방장은 방을 나갈 수 없습니다."),
    FULL_MEMBER(false, 2012, "그룹의 인원이 모두 찼습니다."),
    EXIST_JOIN_GROUP(false,2013, "이미 참여된 방이 있는 유저입니다."),




    /**
     * 3000 : Response 오류
     */
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),

    SCHEDULER_ERROR(false, 4001, "스케줄러 오류가 발생했습니다."),
    SSE_EMITTER_NOT_FOUND(false, 4002, "해당 GROUP_ID에 대한 SSE 연결을 찾을 수 없습니다.")
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
