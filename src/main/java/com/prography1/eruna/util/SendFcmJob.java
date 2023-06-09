package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
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
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;


@Component
@RequiredArgsConstructor
public class SendFcmJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SendFcmJob.class);
//    private final Scheduler scheduler;
    private final UserService userService;
    private final GroupUserRepository groupUserRepository;
    private final WakeUpCacheRepository wakeUpCacheRepository;


    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Groups group = (Groups) jobDataMap.get("group");
        List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);

        for (GroupUser groupUser : groupUsers) {
            User user = groupUser.getUser();
            String nickname = groupUser.getNickname();

            UserResDto.WakeupDto wakeupDto = UserResDto.WakeupDto.fromUser(user, nickname);
            wakeUpCacheRepository.addSleepUser(group.getId(), wakeupDto);

            String fcmToken = user.getFcmToken();
            log.info("push message schedule is executed : " + fcmToken);
            userService.pushMessage(fcmToken);
        }
    }

    public static Trigger setFcmJobTrigger(LocalTime localTime){
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
//        localDateTime.plusMinutes(1);
        Date date = java.sql.Timestamp.valueOf(localDateTime);
        return TriggerBuilder.newTrigger()
                .startAt(date).build();
    }
}
