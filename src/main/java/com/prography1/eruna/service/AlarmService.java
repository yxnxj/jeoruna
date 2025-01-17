package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.util.scheduler.SchedulerManager;
import com.prography1.eruna.util.scheduler.job.JobWithDataConfig;
import com.prography1.eruna.util.scheduler.job.SendFcmJob;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {
    private final SchedulerManager schedulerManager;
    private final GroupUserRepository groupUserRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final WakeUpCacheRepository wakeUpCacheRepository;

    public void editAlarmScheduleNow(Alarm alarm, Groups group, List<DayOfWeek> days) {
        createAlarmScheduleInGroup(alarm, group);
    }

    public void addAlarmScheduleOnCreate(Alarm alarm, GroupUser groupUser, List<DayOfWeek> days) {
        if(!isValidAlarmAtTimeAndDay(alarm, days)) return;

        User host = groupUser.getUser();
        UserResDto.WakeupDto wakeupDto = UserResDto.WakeupDto.fromUser(host, groupUser.getNickname(), groupUser.getPhoneNum());
        wakeUpCacheRepository.addSleepUser(groupUser.getGroups().getId(), wakeupDto);

        createAlarmSchedule(alarm, host);
    }

    private boolean isTodayAlarm(List<DayOfWeek> days){
        LocalDate localDate = LocalDate.now();
        String day = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);

        for (DayOfWeek storedDay : days){
            if(day.equals(storedDay.getDayOfWeekId().getDay().toString())) {
                log.info("Day: " + day);
                return true;
            }
        }

        return false;
    }

    private boolean isAfterAlarmFromNow(Alarm alarm){
        LocalTime time = alarm.getAlarmTime();
        if(time.isAfter(LocalTime.now())){
            return true;
        }
        return false;
    }

    private boolean isValidAlarmAtTimeAndDay(Alarm alarm, List<DayOfWeek> days){
        return isAfterAlarmFromNow(alarm) && isTodayAlarm(days);
    }

    private boolean isValidAlarmAtTimeAndDay(Alarm alarm){
        /**
         * alarm 생성 후 alarm.weeklist가 연결되어 있지 않은 alarm entity가
         * persistence context에 저장되어 있지 않은 상황을 대비해
         * repository에서 찾아서 사용한다.
         */
        List<DayOfWeek> days = dayOfWeekRepository.findAllByAlarm(alarm);

        return isValidAlarmAtTimeAndDay(alarm, days);
    }

    public void createAlarmScheduleInGroup(Alarm alarm) {
        createAlarmScheduleInGroup(alarm, alarm.getGroups());
    }

    public void createAlarmScheduleInGroup(Alarm alarm, Groups group) {
        List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);
        createGroupUsersSchedule(groupUsers, alarm);
    }

    private void createGroupUsersSchedule(List<GroupUser> groupUsers, Alarm alarm) {
        if (!isValidAlarmAtTimeAndDay(alarm)) return;
        for (GroupUser groupUser : groupUsers) {
            User user = groupUser.getUser();
            String nickname = groupUser.getNickname();
            String phoneNum = groupUser.getPhoneNum();
            UserResDto.WakeupDto wakeupDto = UserResDto.WakeupDto.fromUser(user, nickname, phoneNum);
            wakeUpCacheRepository.addSleepUser(groupUser.getGroups().getId(), wakeupDto);
            createAlarmSchedule(alarm, user);
        }
    }

    private void createAlarmSchedule(Alarm alarm, User user){
        JobDataMap mapData = SendFcmJob.mapDataForFCMJob(alarm, user);
        JobWithDataConfig jobConfig = JobWithDataConfig.builder()
                .jobClass(SendFcmJob.class)
                .jobDataMap(mapData)
                .trigger(SendFcmJob.setJobTrigger(alarm.getAlarmTime(), LocalDate.now()))
                .build();

        String identity = user.getUuid();
        log.info("__________Schedule__________");
        log.info("identity : " + identity + ", alarm : " + jobConfig.getTrigger().getStartTime());
        schedulerManager.deleteIfExist(JobKey.jobKey(identity));
        schedulerManager.createSchedule(jobConfig, identity);
    }
}
