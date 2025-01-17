package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.exception.badstate.*;
import com.prography1.eruna.exception.invalid.InvalidGroupCodeException;
import com.prography1.eruna.exception.notfound.AlarmNotFoundException;
import com.prography1.eruna.exception.notfound.GroupNotFoundException;
import com.prography1.eruna.exception.notfound.GroupUserNotFoundException;
import com.prography1.eruna.exception.notfound.UserNotFoundException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.web.GroupResDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.prography1.eruna.response.BaseResponseStatus.*;
import static com.prography1.eruna.web.GroupReqDto.*;
import static com.prography1.eruna.web.GroupResDto.GroupPreview;

@RequiredArgsConstructor
@Transactional
@Service
public class GroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final GroupUserRepository groupUserRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final WakeUpCacheRepository wakeUpCacheRepository;
    private final AlarmService alarmService;

    public GroupResDto.CreatedGroup createGroup(CreateGroup createGroup) {
        User host = userRepository.findByUuid(createGroup.getUuid())
                .orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", createGroup.getUuid())));
        if(groupUserRepository.existsByUser(host)){
            GroupUser user = groupUserRepository.findGroupUserByUser(host).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", createGroup.getUuid())));
            throw new ExistJoinGroupException(EXIST_JOIN_GROUP, String.format("`%s` user는 %d group에 이미 속해있습니다.", host.getUuid(), user.getGroups().getId()));
        }
        AlarmInfo alarmInfo = createGroup.getAlarmInfo();
        Groups group = Groups.create(host);
        Alarm alarm= alarmInfoToAlarm(alarmInfo, group);
        GroupUser groupUser = GroupUser.builder().user(host).groups(group).nickname(createGroup.getNickname())
                .phoneNum(createGroup.getPhoneNum())
                .groupUserId(GroupUser.GroupUserId.builder().groupId(group.getId()).userId(host.getId()).build())
                .build();
        List<DayOfWeek> dayOfWeekList  = new ArrayList<>();
        for(Week week  :createGroup.getAlarmInfo().getWeek()){
            DayOfWeek.DayOfWeekId dayOfWeekId = new DayOfWeek.DayOfWeekId(alarm.getId(), week);
            DayOfWeek dayOfWeek = new DayOfWeek(dayOfWeekId, alarm);
            dayOfWeekList.add(dayOfWeek);
        }
        groupRepository.save(group);
        alarmRepository.save(alarm);

        alarmService.addAlarmScheduleOnCreate(alarm, groupUser, dayOfWeekList);

        groupUserRepository.save(groupUser);
        for(DayOfWeek dayOfWeek : dayOfWeekList){
            dayOfWeekRepository.save(dayOfWeek);
        }
        return new GroupResDto.CreatedGroup(group.getId(), group.getCode());
    }

    private Alarm alarmInfoToAlarm(AlarmInfo alarmInfo, Groups group){
        LocalTime alarmTime = LocalTime.of(alarmInfo.getHours(), alarmInfo.getMinutes());
        return Alarm.builder()
                .groups(group).startDate(LocalDate.now()).finishDate(LocalDate.of(2999,12,31))
                .alarmTime(alarmTime).alarmSound(AlarmSound.valueOf(alarmInfo.getSound()))
                .build();
    }

    public Boolean isValidCode(String code){
        return groupRepository.existsByCode(code);
    }

    public Groups findByCode(String code){
        return groupRepository.findByCode(code).orElseThrow(()-> new InvalidGroupCodeException(BaseResponseStatus.INVALID_GROUP_CODE, String.format("GroupCode `%s`가 존재하지 않습니다.", code)));
    }

    public boolean isDuplicatedNickname(String code, String nickname) {
        Groups group = findByCode(code);
        return groupUserRepository.existsByGroupsAndNickname(group, nickname);
    }

    public Long joinGroupUser(String code, String uuid, String nickname, String phoneNum){
        Groups group = findByCode(code);
        User user = userRepository.findByUuid(uuid).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", uuid)));
        if(groupUserRepository.existsByUser(user)){
            GroupUser gu = groupUserRepository.findGroupUserByUser(user).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", user.getUuid())));
            throw new ExistJoinGroupException(EXIST_JOIN_GROUP, String.format("`%s` user는 %d group에 이미 속해있습니다.", user.getUuid(), gu.getGroups().getId()));
        }
        GroupUser.GroupUserId groupUserId = GroupUser.GroupUserId.builder()
                .groupId(group.getId())
                .userId(user.getId())
                .build();
        GroupUser groupUser = GroupUser.builder()
                .groupUserId(groupUserId)
                .user(user)
                .groups(group)
                .nickname(nickname)
                .phoneNum(phoneNum)
                .build();
        Alarm alarm = alarmRepository.findByGroups(group).orElseThrow(() -> new AlarmNotFoundException(NOT_FOUND_ALARM, String.format("%d 그룹의 알람이 존재하지 않습니다. ", group.getId())));
        List<DayOfWeek> dayOfWeekList = dayOfWeekRepository.findAllByAlarm(alarm);

        alarmService.addAlarmScheduleOnCreate(alarm, groupUser, dayOfWeekList);

        groupUserRepository.save(groupUser);
        return group.getId();
    }

    public Groups findGroupById(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%d group을 찾을 수 없습니다.", groupId)));
    }


    public void kickMember(Long groupId, String nickname, String hostUuid) {
        User host = userRepository.findByUuid(hostUuid).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", hostUuid)));
        Groups group = groupRepository.findById(groupId).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%d group을 찾을 수 없습니다.", groupId)));
        if(!isHost(group, host)){
            throw new NotHostException(NOT_HOST);
        }
        GroupUser kickedMember = groupUserRepository.findByNickname(nickname)
                .orElseThrow(() -> new GroupUserNotFoundException(NOT_FOUND_GROUP_USER, String.format("%d group에 %s nickname을 갖는 user를 찾을 수 없습니다.", groupId, nickname)));

        groupUserRepository.delete(kickedMember);
    }

    private boolean isHost(Groups group, User user) {
        return group.getHost() == user;
    }

    public void editAlarm(Long groupId, AlarmEdit alarmEdit) {
        User host = userRepository.findByUuid(alarmEdit.getUuid()).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", alarmEdit.getUuid())));
        Groups group = groupRepository.findById(groupId).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%d group을 찾을 수 없습니다.", groupId)));
        if(!isHost(group, host)){
            throw new NotHostException(NOT_HOST);
        }
        LocalTime newTime = LocalTime.of(alarmEdit.getAlarmInfo().getHours(), alarmEdit.getAlarmInfo().getMinutes());
        group.getAlarm().update(AlarmSound.valueOf(alarmEdit.getAlarmInfo().getSound()), newTime);

        //기존 요일 삭제
        List<DayOfWeek> oldDayOfWeekList = dayOfWeekRepository.findAllByAlarm(group.getAlarm());
        for (DayOfWeek oldDay : oldDayOfWeekList) {
            dayOfWeekRepository.delete(oldDay);
        }

        // 요일리스트 생성 및 저장
        List<DayOfWeek> newDayOfWeekList = new ArrayList<>();
        for (Week week : alarmEdit.getAlarmInfo().getWeek()) {
            DayOfWeek.DayOfWeekId dayOfWeekId = new DayOfWeek.DayOfWeekId(group.getAlarm().getId(), week);
            DayOfWeek dayOfWeek = new DayOfWeek(dayOfWeekId, group.getAlarm());
            newDayOfWeekList.add(dayOfWeek);
        }
        for (DayOfWeek dayOfWeek : newDayOfWeekList) {
            dayOfWeekRepository.save(dayOfWeek);
        }


        alarmService.editAlarmScheduleNow(group.getAlarm(), group, newDayOfWeekList);

    }

    public boolean isUserExistInGroup(String uuid, String code){
        User user = userRepository.findByUuid(uuid).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", uuid)));
        Groups group = groupRepository.findByCode(code).orElseThrow(()-> new InvalidGroupCodeException(BaseResponseStatus.INVALID_GROUP_CODE, String.format("GroupCode `%s`가 존재하지 않습니다.", code)));
        return groupUserRepository.existsByGroupsAndUser(group, user);
//        return userRepository.existsByUuid(uuid);
    }

    public String reissueGroupCode(Long groupId, String hostUuid) {
        User host = userRepository.findByUuid(hostUuid).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", hostUuid)));
        Groups group = groupRepository.findById(groupId).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%d group을 찾을 수 없습니다.", groupId)));
        if(!isHost(group, host)){
            throw new NotHostException(NOT_HOST);
        }
        group.changeCode();
        groupRepository.save(group);
        return group.getCode();
    }

    public Integer groupMemberCountByCode(String code) {
        Groups group = groupRepository.findByCode(code).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%s group을 찾을 수 없습니다.", code)));
        return groupUserRepository.countByGroups(group).intValue();
    }

    public String getHostNicknameByGroupCode(String code) {
        Groups group = groupRepository.findByCode(code).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%s group을 찾을 수 없습니다.", code)));
        User host = group.getHost();
        GroupUser groupUser = groupUserRepository.findGroupUserByUser(host).orElseThrow(() -> new GroupUserNotFoundException(NOT_FOUND_GROUP_USER, String.format("%d group에 %s uuid를 갖는 user를 찾을 수 없습니다.", group.getId(), host.getUuid())));
        return groupUser.getNickname();
    }

    public void exitGroup(Long groupId, String uuid) {
        User user = userRepository.findByUuid(uuid).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", uuid)));
        Groups group = groupRepository.findById(groupId).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%d group을 찾을 수 없습니다.", groupId)));
        GroupUser groupUser =
                groupUserRepository.findGroupUserByUser(user).orElseThrow(() -> new GroupUserNotFoundException(NOT_FOUND_GROUP_USER, String.format("%d group에 %s uuid를 갖는 user를 찾을 수 없습니다.", group.getId(), user.getUuid())));
        if(isHost(group, user)){
            throw new HostCannotExitException(HOST_CANNOT_EXIT);
        }
        if(Objects.equals(groupUser.getGroups().getId(), group.getId())) {
            groupUserRepository.delete(groupUser);
        }else{
            throw new GroupUserNotFoundException(NOT_FOUND_GROUP_USER, String.format("%d group에 %s uuid를 갖는 user를 찾을 수 없습니다.", group.getId(), uuid));
        }
    }

    public void deleteGroup(Long groupId, String uuid) {
        User user = userRepository.findByUuid(uuid).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", uuid)));
        Groups group = groupRepository.findById(groupId).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%d group을 찾을 수 없습니다.", groupId)));
        if(!isHost(group, user)){
            throw new NotHostException(NOT_HOST);
        }
        groupRepository.delete(group);
    }

    public boolean isFullMember(String code) {
        if(groupMemberCountByCode(code)>3) {
            return true;
        }else{
            return false;
        }
    }

    public void checkUserJoinException(String code, String uuid, String nickname) {
        if(!this.isValidCode(code)) throw new InvalidGroupCodeException(BaseResponseStatus.INVALID_GROUP_CODE, String.format("GroupCode `%s`가 존재하지 않습니다.", code));
        if(!userRepository.existsByUuid(uuid)) throw new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("%s uuid를 갖는 user를 찾지 못했습니다.", uuid));
        if(this.isUserExistInGroup(uuid, code))
            throw new AlreadyGroupUserException(BaseResponseStatus.ALREADY_IN_GROUP_USER);
        if(this.isDuplicatedNickname(code, nickname)) throw new DuplicationNicknameException(BaseResponseStatus.DUPLICATED_NICKNAME, String.format("`%s 닉네임은 이미 존재합니다.", nickname));

    }

    public GroupPreview findGroupPreview(String code){
        Integer groupMemberCount = groupMemberCountByCode(code);
        String hostNickname = getHostNicknameByGroupCode(code);
        return new GroupResDto.GroupPreview(groupMemberCount, hostNickname);
    }

    public boolean isActiveGroupCode(String code){
        if(!isValidCode(code)) throw new InvalidGroupCodeException(BaseResponseStatus.INVALID_GROUP_CODE, String.format("GroupCode `%s`가 존재하지 않습니다.", code));
        if(isFullMember(code)) throw new FullMemberException(BaseResponseStatus.FULL_MEMBER);

        return true;
    }
}
