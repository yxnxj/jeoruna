package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.prography1.eruna.web.GroupReqDto.*;
import static com.prography1.eruna.response.BaseResponseStatus.*;

@RequiredArgsConstructor
@Transactional
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final GroupUserRepository groupUserRepository;
    private final DayOfWeekRepository dayOfWeekRepository;

    public Long createGroup(CreateGroup createGroup) {
        AlarmInfo alarmInfo = createGroup.getAlarmInfo();
        User host = userRepository.findByUuid(createGroup.getUuid())
                .orElseThrow(() -> new BaseException(INVALID_UUID_TOKEN));
        Groups group = Groups.create(host, alarmInfo.getPenalty());
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
        groupUserRepository.save(groupUser);
        for(DayOfWeek dayOfWeek : dayOfWeekList){
            dayOfWeekRepository.save(dayOfWeek);
        }
        return group.getId();
    }

    private Alarm alarmInfoToAlarm(AlarmInfo alarmInfo, Groups group){
        LocalTime alarmTime = LocalTime.of(alarmInfo.getHours(), alarmInfo.getMinutes());
        return Alarm.builder()
                .groups(group).startDate(LocalDate.now()).finishDate(LocalDate.of(2999,12,31))
                .alarmTime(alarmTime).alarmSound(alarmInfo.getSound())
                .build();
    }

    public Boolean isValidCode(String code){
        return groupRepository.existsByCode(code);
    }

    public Groups findByCode(String code){
        return groupRepository.findByCode(code).orElseThrow(()-> new BaseException(BaseResponseStatus.INVALID_GROUP_CODE));
    }

    public boolean isDuplicatedNickname(String code, String nickname) {
        Groups group = findByCode(code);
        return groupUserRepository.existsByGroupsAndNickname(group, nickname);
    }

    public GroupUser joinGroupUser(String code, String uuid, String nickname, String phoneNum){
        Groups group = findByCode(code);
        User user = userRepository.findByUuid(uuid).orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));
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
        return groupUserRepository.save(groupUser);
    }

}
