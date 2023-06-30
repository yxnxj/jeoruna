package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.domain.repository.GroupUserRepository;
import com.prography1.eruna.domain.repository.WakeUpCacheRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.util.JobCompletionNotificationListener;
import com.prography1.eruna.util.SendFcmJob;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.prography1.eruna.util.SendFcmJob.setFcmJobTrigger;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final Scheduler scheduler;
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmService.class);
    private final GroupUserRepository groupUserRepository;
    private final WakeUpCacheRepository wakeUpCacheRepository;

    public void editAlarmScheduleNow(Alarm alarm, Groups group, List<DayOfWeek> dayOfWeeks) throws SchedulerException {
        if(!isTodayAlarm(dayOfWeeks)) return;

        List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);

        for (GroupUser groupUser : groupUsers) {
            User user = groupUser.getUser();
            String nickname = groupUser.getNickname();
            String phoneNum = groupUser.getPhoneNum();
            UserResDto.WakeupDto wakeupDto = UserResDto.WakeupDto.fromUser(user, nickname, phoneNum);
            wakeUpCacheRepository.addSleepUser(group.getId(), wakeupDto);

            JobKey jobKey = JobKey.jobKey(user.getUuid());
            scheduler.deleteJob(jobKey);

            createJob(alarm, user);
        }
    }
    public void addAlarmScheduleOnCreate(Alarm alarm, GroupUser groupUser, List<DayOfWeek> days) throws SchedulerException {
        if (!isTodayAlarm(days)) return;

        User host = groupUser.getUser();
        UserResDto.WakeupDto wakeupDto = UserResDto.WakeupDto.fromUser(host, groupUser.getNickname(), groupUser.getPhoneNum());
        wakeUpCacheRepository.addSleepUser(groupUser.getGroups().getId(), wakeupDto);

        createJob(alarm, host);

    }

    private void createJob(Alarm alarm, User user) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fcmToken", user.getFcmToken());

        JobDetail job = JobBuilder
                .newJob(SendFcmJob.class)
                .withIdentity(user.getUuid())
                .usingJobData(jobDataMap)
                .build();
        LOGGER.info("__________Schedule__________");
        LOGGER.info("group : " + alarm.getGroups().getId() + ", alarm : " + alarm.getAlarmTime());
        scheduler.scheduleJob(job, setFcmJobTrigger(alarm.getAlarmTime()));
    }

    private boolean isTodayAlarm(List<DayOfWeek> days){
        LocalDate localDate = LocalDate.now();
        String day = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);

        for (DayOfWeek storedDay : days){
            if(day.equals(storedDay.getDayOfWeekId().getDay().toString())) {
                LOGGER.info("Day: " + day);
                return true;
            }
        }

        return false;
    }

}
