package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.domain.repository.GroupUserRepository;
import com.prography1.eruna.domain.repository.WakeUpCacheRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.UserService;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;


@Component
@RequiredArgsConstructor
public class SendFcmJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SendFcmJob.class);
//    private final Scheduler scheduler;
    private final UserService userService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String fcmToken =  jobDataMap.getString("fcmToken");
        AlarmSound alarmSound = AlarmSound.valueOf(jobDataMap.getString("alarmSound"));
        log.info("push message schedule is executed : " + fcmToken);

        userService.pushMessage(fcmToken, alarmSound.getFilename());
    }

    public static Trigger setFcmJobTrigger(LocalTime localTime){
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
//        localDateTime.plusMinutes(1);
        Date date = java.sql.Timestamp.valueOf(localDateTime);
        return TriggerBuilder.newTrigger()
                .startAt(date)
                //FCM 무한 전송의 limit 시간은 20분이다. 2초 간격으로 600번의 요청을 보낸다.
                .withSchedule(simpleSchedule().withRepeatCount(600).withIntervalInMilliseconds(2000))
                .build();
    }
}
