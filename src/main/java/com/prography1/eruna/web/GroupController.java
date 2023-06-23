package com.prography1.eruna.web;

import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponse;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.GroupService;
import com.prography1.eruna.service.UserService;
import com.prography1.eruna.util.SseEmitters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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
    private final SseEmitters sseEmitters;
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

    @Operation(summary = "새 유저 그룹 합류", description = "그룹 링크를 공유받은 유저가 그룹에 참여한다.")
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

    @Operation(summary = "닉네임 중복 확인", description = "참여하려는 그룹에 중복된 닉네임이 있는지 확인한다.")
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

//    @Operation(summary = "알람 정보 수정", description = "알람 정보 수정")
//    @PatchMapping("/{groupId}/alarm")
//    public BaseResponse<String> editAlarm(@PathVariable Long groupId, @RequestBody AlarmEdit alarmEdit){
//        groupService.editAlarm(groupId, alarmEdit);
//        return new BaseResponse<>("ok");
//    }


    @Operation(summary = "그룹 멤버 강퇴", description = "그룹 멤버 강퇴")
    @PatchMapping("/{groupId}/kick/{nickname}")
    public BaseResponse<String> kickMember(@PathVariable Long groupId, @PathVariable String nickname,
                                           @RequestBody KickMember kickMember){
        groupService.kickMember(groupId, nickname, kickMember.getUuid());
        return new BaseResponse<>("ok");
    }


    @Operation(summary = "그룹 기상 정보 페이지 접속", description = "유저들의 기상 정보 확인 API \n SSE 연결 수행 및 캐싱된 기상 정보를 반환한다.")
    @GetMapping("/wake-up/{groupId}")
    public BaseResponse<List<UserResDto.WakeupDto>> sendWakeupInfo(@PathVariable Long groupId){
        SseEmitter emitter = new SseEmitter(60*1000L);
        sseEmitters.add(groupId, emitter);


        return new BaseResponse<>(sseEmitters.sendWakeupInfo(groupId, emitter));
//        return ResponseEntity.ok(emitter);
    }

    @Operation(summary = "유저 기상", description = "캐싱된 기상정보 데이터들을 업데이트 한다.")
    @PostMapping("/wake-up/{groupId}/{uuid}")
    public BaseResponse<List<UserResDto.WakeupDto>> userWakeup(@PathVariable Long groupId, @PathVariable String uuid){
        groupService.updateWakeupInfo(groupId, uuid);
        return new BaseResponse<>(sseEmitters.sendWakeupInfo(groupId));
    }
}
