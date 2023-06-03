package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.repository.GroupUserRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.UserService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@RequiredArgsConstructor
class SendFcmJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SendFcmJob.class);
//    private final Scheduler scheduler;
    private final UserService userService;
    private final GroupUserRepository groupUserRepository;


    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Groups group = (Groups) jobDataMap.get("group");
        List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);

        for (GroupUser groupUser : groupUsers) {
            User user = groupUser.getUser();
            String fcmToken = user.getFcmToken();

            log.info("push message schedule is executed : " + fcmToken);
            userService.pushMessage(fcmToken);
        }

    }
}
