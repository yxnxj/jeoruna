package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.enums.Penalty;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.util.JobCompletionNotificationListener;
import lombok.RequiredArgsConstructor;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.prography1.eruna.web.GroupReqDto.*;
import static com.prography1.eruna.response.BaseResponseStatus.*;

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
    private final Scheduler scheduler;

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

    public List<String> findPenaltyList() {
        List<String> penaltyList = new ArrayList<>();
        Arrays.stream(Penalty.values()).forEach(item -> penaltyList.add(item.getDetail()));
        return penaltyList;
    }

    public Groups findGroupById(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(()-> new BaseException(NOT_FOUND_GROUP));
    }

    public void updateWakeupInfo(Long groupId, String uuid){
        try {
            deleteFcmJob(uuid);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        User user = userRepository.findByUuid(uuid).orElseThrow( () -> new BaseException(USER_NOT_FOUND));
        GroupUser groupUser = groupUserRepository.findGroupUserByUser(user).orElseThrow(() -> new BaseException(NOT_FOUND_GROUP));
        wakeUpCacheRepository.updateWakeupInfo(groupId, uuid, groupUser.getNickname());
    }
    public void kickMember(Long groupId, String nickname, String hostUuid) {
        User host = userRepository.findByUuid(hostUuid).orElseThrow(() -> new BaseException(INVALID_UUID_TOKEN));
        Groups group = groupRepository.findById(groupId).orElseThrow(() -> new BaseException(NOT_FOUND_GROUP));
        if(!isHost(group, host)){
            throw new BaseException(NOT_HOST);
        }
        GroupUser kickedMember = groupUserRepository.findByNickname(nickname)
                .orElseThrow(() -> new BaseException(NOT_FOUND_GROUP_USER));

        groupUserRepository.delete(kickedMember);
    }

    private boolean isHost(Groups group, User user) {
        return group.getHost() == user;
    }

    private void deleteFcmJob(String uuid) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(uuid);
        scheduler.deleteJob(jobKey);
        LOGGER.info("fcm job delete : " + uuid);
    }
}
