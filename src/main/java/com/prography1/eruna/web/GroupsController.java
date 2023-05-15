package com.prography1.eruna.web;

import com.prography1.eruna.response.BaseResponse;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.GroupsService;
import com.prography1.eruna.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupsController {
    private final GroupsService groupsService;
    private final UserService userService;

    @PostMapping("/{code}")
    public BaseResponse<String> userJoinGroup(@PathVariable String code, @RequestBody GroupsReqDto.GroupJoinUserInfo groupJoinUserInfo){
        if(!groupsService.isValidCode(code)) return new BaseResponse<>(BaseResponseStatus.INVALID_GROUP_CODE);

        String uuid = groupJoinUserInfo.getUuid();
        if(!userService.isUserExist(uuid)) return new BaseResponse<>(BaseResponseStatus.INVALID_UUID_TOKEN);
        String nickname = groupJoinUserInfo.getNickname();
        if(groupsService.isDuplicatedNickname(code, nickname)) return new BaseResponse<>(BaseResponseStatus.DUPLICATED_NICKNAME);

        groupsService.joinGroupUser(code, uuid, nickname, groupJoinUserInfo.getPhoneNum());
        return new BaseResponse("ok");
    }
}
