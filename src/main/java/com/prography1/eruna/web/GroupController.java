package com.prography1.eruna.web;

import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponse;
import com.prography1.eruna.service.GroupService;
import com.prography1.eruna.service.UserService;
import com.prography1.eruna.service.WakeupService;
import com.prography1.eruna.util.SseEmitters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    private final WakeupService wakeupService;
    private final SseEmitters sseEmitters;
    @Operation(summary = "그룹 만들기", description = "알람 그룹 만들기")
    @PostMapping("")
    public BaseResponse<CreatedGroup> createGroup(@RequestBody CreateGroup createGroup){
//        CreatedGroup createdGroup = new CreatedGroup(groupService.createGroup(createGroup));
        return new BaseResponse<>(groupService.createGroup(createGroup));
    }

    @Operation(summary = "새 유저 그룹 합류", description = "그룹 링크를 공유받은 유저가 그룹에 참여한다.",
            responses =
            @ApiResponse(responseCode = "200", description = "닉네임 유효 확인",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)
                            ,examples = {
                            @ExampleObject("""
                                    {
                                      "isSuccess": true,
                                      "code": 1000,
                                      "message": "요청에 성공하였습니다.",
                                      "result": "1"
                                    }""")}
                    )))
    @PostMapping("/{code}")
    public BaseResponse<Long> userJoinGroup(@PathVariable String code, @RequestBody GroupJoinUserInfo groupJoinUserInfo){
        String nickname = groupJoinUserInfo.getNickname();
        String uuid = groupJoinUserInfo.getUuid();
        groupService.checkUserJoinException(code, uuid, nickname);
        return new BaseResponse<>(groupService.joinGroupUser(code, uuid, nickname, groupJoinUserInfo.getPhoneNum()));
    }


    @ExceptionHandler(BaseException.class)
    public BaseResponse<String> handleBaseException(BaseException e) {
        log.info(e.getStatus().toString());
        return new BaseResponse<>(e.getStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<String> handleRuntimeException(RuntimeException e) {
        log.info(e.getMessage());
        return new BaseResponse<>(e.getMessage());
    }

    @Operation(summary = "닉네임 중복 확인", description = "참여하려는 그룹에 중복된 닉네임이 있는지 확인한다.",
            responses =
            @ApiResponse(responseCode = "200", description = "닉네임 유효 확인",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)
                            ,examples = {
                            @ExampleObject("""
                                    {
                                      "isSuccess": true,
                                      "code": 1000,
                                      "message": "요청에 성공하였습니다.",
                                      "result": "true"
                                    }""")}
                    )))
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

    @Operation(summary = "알람 정보 수정", description = "알람 정보 수정")
    @PatchMapping("/{groupId}/alarm")
    public BaseResponse<String> editAlarm(@PathVariable Long groupId, @RequestBody AlarmEdit alarmEdit){
        groupService.editAlarm(groupId, alarmEdit);
        return new BaseResponse<>("ok");
    }

    @Operation(summary = "그룹 코드 재생성", description = "그룹 코드 재생성")
    @PatchMapping("/{groupId}/refresh-code")
    public BaseResponse<NewGroupCode> refreshGroupCode(@PathVariable Long groupId, @RequestBody UUID uuid){
        String newCode = groupService.reissueGroupCode(groupId, uuid.getUuid());
        return new BaseResponse<>(new NewGroupCode(newCode));
    }

    @Operation(summary = "그룹 코드 validation", description = "그룹 코드 validation")
    @GetMapping("/{code}/validation")
    public BaseResponse<String> checkGroupCode(@PathVariable String code){
        if(groupService.isActiveGroupCode(code))
            return new BaseResponse<>("ok");
        return new BaseResponse<>("false");
    }


    @Operation(summary = "그룹 멤버 강퇴", description = "그룹 멤버 강퇴")
    @PatchMapping("/{groupId}/kick/{nickname}")
    public BaseResponse<String> kickMember(@PathVariable Long groupId, @PathVariable String nickname,
                                           @RequestBody KickMember kickMember){
        groupService.kickMember(groupId, nickname, kickMember.getUuid());
        return new BaseResponse<>("ok");
    }

    @Operation(summary = "그룹 멤버 수, 방장 닉네임 반환", description = "그룹 코드를 받으면 해당 그룹의 멤버 수, 방장 닉네임 반환")
    @GetMapping("/{code}/preview")
    public BaseResponse<GroupPreview> groupPreview(@PathVariable String code){
        return new BaseResponse<>(groupService.findGroupPreview(code));
    }

    @Operation(summary = "그룹 나가기", description = "그룹 나가기")
    @PostMapping("/{groupId}/exit")
    public BaseResponse<String> exitGroup(@PathVariable Long groupId, @RequestBody UUID uuid){
        groupService.exitGroup(groupId, uuid.getUuid());
        return new BaseResponse<>("ok");
    }

    @Operation(summary = "그룹 삭제", description = "그룹 삭제")
    @PostMapping("/{groupId}/delete")
    public BaseResponse<String> deleteGroup(@PathVariable Long groupId, @RequestBody UUID uuid){
        groupService.deleteGroup(groupId, uuid.getUuid());
        return new BaseResponse<>("ok");
    }

  @Operation(summary = "그룹 기상 정보 페이지 접속", description = "유저들의 기상 정보 확인 API \n SSE 연결 수행 및 캐싱된 기상 정보를 반환한다.",
            responses = @ApiResponse(responseCode = "200", description = "SSE 연결 및 캐싱 완료 \n 그룹에 포함된 유저들의 기상정보를 리스트 형태로 반환한다.", content = @Content(array= @ArraySchema(schema = @Schema(implementation = UserResDto.WakeupDto.class))
            ,examples = {
                    @ExampleObject("""
                     {
                      "isSuccess": true,
                      "code": 1000,
                      "message": "요청에 성공하였습니다.",
                      "result":
                            [" {
                                "uuid": "6e383010-7621-437b-98d5-fe2147465ac0",
                                "nickname": "user name1",
                                "wakeup": false,
                                "wakeupTime": "15:19:47.459",
                                "phoneNum" : "01000000000"
                            }", " {
                                "uuid": "fe214749-4321-437b-54d1-fe216e383010",
                                "nickname": "user name2",
                                "wakeup": true,
                                "wakeupTime": "21:19:47.459",
                                "phoneNum" : "01000000000"
                            }"]""")
            })))
    @GetMapping(value = "/wake-up/{groupId}")
    public BaseResponse<List<UserResDto.WakeupDto>> sendWakeupInfo(@PathVariable Long groupId){
//      sseEmitters.add(groupId);
//      sseEmitters.sendWakeupInfo(groupId, uuid);
      return new BaseResponse<>(wakeupService.findWakeupInfo(groupId));
    }

//    @CrossOrigin
//    @GetMapping(value = "/sse/{groupId}/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public ResponseEntity<SseEmitter> connect(HttpServletResponse response, @PathVariable Long groupId, @PathVariable String uuid) {
//        SseEmitter emitter = sseEmitters.add(groupId, uuid);
//        response.addHeader("X-Accel-Buffering", "no");
//        sseEmitters.sendWakeupInfo(groupId, uuid);
//        return new ResponseEntity<>(emitter, HttpStatus.OK);
//    }


    @Operation(summary = "유저 기상", description = "캐싱된 기상정보 데이터들을 업데이트 한다.",
            responses =
            @ApiResponse(responseCode = "200", description = "SSE 연결 및 캐싱 완료 \n 그룹에 포함된 유저들의 기상정보를 리스트 형태로 반환한다.",
                    content = @Content(array= @ArraySchema(schema = @Schema(title = "유저 기상 정보", implementation = UserResDto.WakeupDto.class))
            ,examples = {
            @ExampleObject("""
                     {
                      "isSuccess": true,
                      "code": 1000,
                      "message": "요청에 성공하였습니다.",
                      "result":
                            [" {
                                "uuid": "6e383010-7621-437b-98d5-fe2147465ac0",
                                "nickname": "user name1",
                                "wakeup": false,
                                "wakeupTime": "15:19:47.459",
                                "phoneNum" : "01000000000"
                            }", " {
                                "uuid": "fe214749-4321-437b-54d1-fe216e383010",
                                "nickname": "user name2",
                                "wakeup": true,
                                "wakeupTime": "21:19:47.459",
                                "phoneNum" : "01000000000"
                            }"]""")
    })))
    @PostMapping("/wake-up/{groupId}")
    public BaseResponse<List<UserResDto.WakeupDto>> userWakeup(@PathVariable Long groupId, @RequestBody UUID uuid){
        wakeupService.updateWakeupInfo(groupId, uuid.getUuid());
//        sseEmitters.sendWakeupInfo2All(groupId);
        return new BaseResponse<>(wakeupService.findWakeupInfo(groupId));
    }
}
