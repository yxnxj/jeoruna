package com.prography1.eruna.web;

import com.prography1.eruna.response.BaseResponse;
import com.prography1.eruna.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.prography1.eruna.web.UserResDto.*;

@RestController
@RequiredArgsConstructor
@Tag(name="User",description = "유저 API")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "UUID 발급으로 회원가입 대체", description = "UUID를 발급하고 회원 등록")
    @GetMapping("/uuid")
    public BaseResponse<UUID> joinByUUID(){
        return new BaseResponse<>(new UUID(userService.joinByUUID()));
    }


}
