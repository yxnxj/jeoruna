package com.prography1.eruna.util.scheduler.job;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.exception.SchedulerException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.UserService;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;


@Component
@RequiredArgsConstructor
public class SendFcmJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SendFcmJob.class);
    private final Scheduler scheduler;
    private final UserService userService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String fcmToken = jobDataMap.getString("fcmToken");
        AlarmSound alarmSound = AlarmSound.valueOf(jobDataMap.getString("alarmSound"));
        log.info("push message schedule is executed : " + fcmToken);

        if (!userService.isValidFCMToken(fcmToken)) {
            String uuid = jobDataMap.getString("uuid");
            JobKey jobKey = JobKey.jobKey(uuid);
            try {
                scheduler.deleteJob(jobKey);
                log.warn("fcmToken : " + fcmToken + "is not valid");
                log.warn("jobKey : " + jobKey.getName() + "schedule is deleted");
            } catch (org.quartz.SchedulerException e) {
                log.error("SCHEDULER ERROR : " + e.getMessage());
                throw new SchedulerException(BaseResponseStatus.SCHEDULER_ERROR);
            }
        }
        userService.pushMessage(fcmToken, alarmSound.getFilename());
    }


    public static JobDataMap mapDataForFCMJob(Alarm alarm, User user) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fcmToken", user.getFcmToken());
        jobDataMap.put("uuid", user.getUuid());
        jobDataMap.put("alarmSound", alarm.getAlarmSound().toString());

        return jobDataMap;
    }


    //FCM 무한 전송의 limit 시간은 20분이다. 2초 간격으로 600번의 요청을 보낸다.
    private final static int repeatCount = 600;
    private final static int interval = 2000;

    public static Trigger setJobTrigger(LocalTime startTime, LocalDate localDate) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, startTime);
        Date date = java.sql.Timestamp.valueOf(localDateTime);

        return TriggerBuilder.newTrigger()
                .startAt(date)
                //FCM 무한 전송의 limit 시간은 20분이다. 2초 간격으로 600번의 요청을 보낸다.
                .withSchedule(simpleSchedule().withRepeatCount(600).withIntervalInMilliseconds(2000))
                .build();
    }


}
