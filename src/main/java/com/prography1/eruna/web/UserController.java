package com.prography1.eruna.web;

import com.prography1.eruna.response.BaseResponse;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.prography1.eruna.web.UserResDto.*;
import static com.prography1.eruna.web.UserReqDto.*;

@RestController
@RequiredArgsConstructor
@Tag(name="User",description = "유저 API")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "UUID 발급으로 회원가입 대체", description = "UUID를 발급하고 회원 등록")
    @PostMapping("/uuid")
    public BaseResponse<UUID> joinByUUID(@RequestBody FcmToken fcmToken){
        return new BaseResponse<>(new UUID(userService.joinByUUID(fcmToken.getFcmToken())));
    }

    @PostMapping("/push")
    public BaseResponse<String> pushMessage(@RequestBody FcmToken fcmToken){
        String token = fcmToken.getFcmToken();
        if(!userService.isValidFCMToken(token))
            return new BaseResponse<>(BaseResponseStatus.INVALID_FCM_TOKEN);

        userService.pushMessage(token);
        return new BaseResponse<>("ok");
    }
}
