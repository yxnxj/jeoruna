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
import com.prography1.eruna.service.AlarmService;
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


//@Component
//@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

//    private static final Logger LOGGER = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
//    private final Scheduler scheduler;
//    private final GroupRepository groupRepository;
//    private final AlarmService alarmService;
//    @Override
////    @Transactional
//    public void afterJob(JobExecution jobExecution) {
//        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
//            try {
//                List<Alarm> alarms = (List<Alarm>) scheduler.getContext().get("alarms");
//
//                if(alarms == null) alarms = new ArrayList<>();
//                for(int i = 0 ; i < alarms.size(); i++){
//                    Alarm alarm = alarms.get(i);
//
//                    Groups group = groupRepository.findByAlarm(alarm).orElseThrow(() -> new BaseException(BaseResponseStatus.DATABASE_ERROR));
//                    alarmService.createAlarmScheduleInGroup(alarm, group);
//                }
//            } catch (SchedulerException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

}
