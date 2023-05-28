package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private final Scheduler scheduler;
    private final GroupRepository groupRepository;
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            try {
                List<Alarm> alarms = (List<Alarm>) scheduler.getContext().get("alarms");
                for(int i = 0 ; i < alarms.size(); i++){
                    Alarm alarm = alarms.get(i);
                    Groups groups = groupRepository.findByAlarm(alarm).orElseThrow(() -> new BaseException(BaseResponseStatus.DATABASE_ERROR));

                    LocalTime time = alarm.getAlarmTime();

                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("group", groups);

                    JobDetail job = JobBuilder
                            .newJob(SendFcmJob.class)
                            .usingJobData(jobDataMap)
                            .build();
                    System.out.println("____________________");
                    System.out.println(groups.getId());
                    scheduler.scheduleJob(job, runJobTrigger(time));
                }
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Trigger runJobTrigger(LocalTime localTime){
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
//        localDateTime.plusMinutes(1);
        Date date = java.sql.Timestamp.valueOf(localDateTime);
        return TriggerBuilder.newTrigger()
                .startAt(date).build();
    }
}
