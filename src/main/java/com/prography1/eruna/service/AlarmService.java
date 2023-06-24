package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.util.JobCompletionNotificationListener;
import com.prography1.eruna.util.SendFcmJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final Scheduler scheduler;
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmService.class);
    private final GroupRepository groupRepository;
    public void addAlarmInSchedule(Alarm alarm) throws SchedulerException {

        Groups groups = groupRepository.findByAlarm(alarm).orElseThrow(() -> new BaseException(BaseResponseStatus.DATABASE_ERROR));

        LocalTime time = alarm.getAlarmTime();
        if(LocalTime.now().isAfter(time)){
            return;
        }

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("group", groups);

        JobDetail job = JobBuilder
                .newJob(SendFcmJob.class)
                .usingJobData(jobDataMap)
                .build();
        LOGGER.info("__________Schedule__________");
        LOGGER.info("group : " + groups.getId() + ", alarm : " + alarm.getAlarmTime());
        scheduler.scheduleJob(job, SendFcmJob.setFcmJobTrigger(time));
    }

}
