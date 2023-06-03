package com.prography1.eruna.web;

import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponse;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.GroupService;
import com.prography1.eruna.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.prography1.eruna.web.GroupReqDto.*;
import static com.prography1.eruna.web.GroupResDto.*;


@RestController
@RequiredArgsConstructor
@Tag(name="Group",description = "알람 그룹 API")
@RequestMapping("/group")
@Slf4j
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    @Operation(summary = "그룹 만들기", description = "알람 그룹 만들기")
    @PostMapping("")
    public BaseResponse<CreatedGroup> createGroup(@RequestBody CreateGroup createGroup){
        CreatedGroup createdGroup = new CreatedGroup(groupService.createGroup(createGroup));
        return new BaseResponse<>(createdGroup);
    }

    @Operation(summary = "패널티 목록 조회", description = "패널티 목록 조회")
    @GetMapping("/penalty-list")
    public BaseResponse<PenaltyList> findPenaltyList(){
        List<String> penaltyList = groupService.findPenaltyList();
        return new BaseResponse<>(new PenaltyList(penaltyList));
    }

    @PostMapping("/{code}")
    public BaseResponse<String> userJoinGroup(@PathVariable String code, @RequestBody GroupJoinUserInfo groupJoinUserInfo){
        if(!groupService.isValidCode(code)) throw new BaseException(BaseResponseStatus.INVALID_GROUP_CODE);
        String uuid = groupJoinUserInfo.getUuid();
        if(!userService.isUserExist(uuid)) throw new BaseException(BaseResponseStatus.INVALID_UUID_TOKEN);
        String nickname = groupJoinUserInfo.getNickname();
        if(groupService.isDuplicatedNickname(code, nickname)) throw new BaseException(BaseResponseStatus.DUPLICATED_NICKNAME);

        groupService.joinGroupUser(code, uuid, nickname, groupJoinUserInfo.getPhoneNum());
        return new BaseResponse<>("ok");
    }


    @ExceptionHandler(BaseException.class)
    public BaseResponse<String> handleBaseException(BaseException e) {
        log.info(e.getClass().toString());
        return new BaseResponse<>(e.getStatus());
    }

    @GetMapping("/{code}/nickname-valid/{nickname}")
    public BaseResponse<GroupResDto.IsValidNickname> isValidNickname(@PathVariable String code, @PathVariable String nickname){
        if(groupService.isDuplicatedNickname(code, nickname)) return new BaseResponse<>(new GroupResDto.IsValidNickname(false));

        return new BaseResponse<>(new GroupResDto.IsValidNickname(true));
    }

    @Operation(summary = "그룹 정보 조회", description = "그룹 정보 조회")
    @GetMapping("/info/{groupId}")
    public BaseResponse<GroupInfo> findGroupInfo(@PathVariable Long groupId){
        Groups group = groupService.findGroupById(groupId);
        return new BaseResponse<>(GroupInfo.fromGroup(group));
    }

    @Operation(summary = "그룹 멤버 강퇴", description = "그룹 멤버 강퇴")
    @PatchMapping("/{groupId}/kick/{nickname}")
    public BaseResponse<String> kickMember(@PathVariable Long groupId, @PathVariable String nickname,
                                           @RequestBody KickMember kickMember){
        groupService.kickMember(groupId, nickname, kickMember.getUuid());
        return new BaseResponse<>("ok");
    }




}
