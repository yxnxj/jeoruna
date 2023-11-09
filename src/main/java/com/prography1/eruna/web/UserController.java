package com.prography1.eruna.web;

import com.prography1.eruna.exception.invalid.InvalidFCMTokenException;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponse;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.prography1.eruna.web.UserReqDto.FcmToken;
import static com.prography1.eruna.web.UserReqDto.RequiredUUID;
import static com.prography1.eruna.web.UserResDto.GroupId;
import static com.prography1.eruna.web.UserResDto.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name="User",description = "유저 API")
@Slf4j
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "UUID 발급으로 회원가입 대체", description = "UUID를 발급하고 회원 등록")
    @PostMapping("/uuid")
    public BaseResponse<UUID> joinByUUID(@RequestBody FcmToken fcmToken){
        return new BaseResponse<>(new UUID(userService.joinByUUID(fcmToken.getFcmToken())));
    }

    @Operation(summary = "UUID로 유저 그룹Id 받아오기", description = "UUID로 유저 그룹Id 받아오기 그룹 없으면 null")
    @PostMapping("/group")
    public BaseResponse<GroupId> getUserGroupId(@RequestBody RequiredUUID uuid){
        return new BaseResponse<>(new GroupId(userService.findGroupIdByUUID(uuid.getUuid())));
    }

    @PostMapping("/push")
    public BaseResponse<String> pushMessage(@RequestBody FcmToken fcmToken){
        String token = fcmToken.getFcmToken();
        if(!userService.isValidFCMToken(token))
            throw new InvalidFCMTokenException(BaseResponseStatus.INVALID_FCM_TOKEN, String.format("`%s token 은 유효하지 않습니다.", token));

        String response = userService.pushMessage(token);
        return new BaseResponse<>(response);
    }


    @ExceptionHandler(BaseException.class)
    public BaseResponse<String> handleBaseException(BaseException e) {
        log.info(e.getStatus().toString());
        return new BaseResponse<>(e.getStatus());
    }
}
