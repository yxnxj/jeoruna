package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.domain.repository.GroupUserRepository;
import com.prography1.eruna.domain.repository.WakeUpCacheRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.UserService;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.prography1.eruna.util.SendFcmJob.setFcmJobTrigger;

@Component
@RequiredArgsConstructor

public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private final Scheduler scheduler;
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final GroupUserRepository groupUserRepository;
    private final WakeUpCacheRepository wakeUpCacheRepository;
    @Override
//    @Transactional
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            try {
                List<Alarm> alarms = (List<Alarm>) scheduler.getContext().get("alarms");

                if(alarms == null) alarms = new ArrayList<>();
                for(int i = 0 ; i < alarms.size(); i++){
                    Alarm alarm = alarms.get(i);

                    LocalTime time = alarm.getAlarmTime();
                    if(LocalTime.now().isAfter(time)){
                        continue;
                    }
                    Groups group = groupRepository.findByAlarm(alarm).orElseThrow(() -> new BaseException(BaseResponseStatus.DATABASE_ERROR));

                    createIndividualSchedule(group, alarm);

                }
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createIndividualSchedule(Groups group, Alarm alarm) throws SchedulerException {
        List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);
        for (GroupUser groupUser : groupUsers) {
            User user = groupUser.getUser();
            String nickname = groupUser.getNickname();
            String phoneNum = groupUser.getPhoneNum();
            UserResDto.WakeupDto wakeupDto = UserResDto.WakeupDto.fromUser(user, nickname, phoneNum);
            wakeUpCacheRepository.addSleepUser(group.getId(), wakeupDto);

            String fcmToken = user.getFcmToken();
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("fcmToken", user.getFcmToken());
            jobDataMap.put("alarmSound", alarm.getAlarmSound());
            JobDetail job = JobBuilder
                    .newJob(SendFcmJob.class)
                    .withIdentity(user.getUuid())
                    .usingJobData(jobDataMap)
                    .build();
            LOGGER.info("__________Schedule__________");
            LOGGER.info("group : " + group.getId() + ", user : " + user.getId() + ", alarm : " + alarm.getAlarmTime());
            scheduler.scheduleJob(job, setFcmJobTrigger(alarm.getAlarmTime()));
        }


    }


}
