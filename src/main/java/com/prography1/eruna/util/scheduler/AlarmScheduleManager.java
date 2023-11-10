package com.prography1.eruna.util.scheduler;

import com.prography1.eruna.exception.SchedulerException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.util.scheduler.job.JobConfig;
import com.prography1.eruna.util.scheduler.job.JobWithDataConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmScheduleManager implements SchedulerManager{

    private final Scheduler scheduler;
    @Override
    public void createSchedule(JobConfig jobConfig, String identity) {
        JobDetail job = JobBuilder
                .newJob(jobConfig.getJobClass())
                .withIdentity(identity)
                .build();

        try{
            scheduler.scheduleJob(job, jobConfig.getTrigger());
        }catch (org.quartz.SchedulerException e){
            log.error("SCHEDULER ERROR : " + e.getMessage());
            throw new org.quartz.SchedulerException(BaseResponseStatus.SCHEDULER_ERROR);
        }
    }

    @Override
    public void createSchedule(JobWithDataConfig jobConfig, String identity) {
        JobDetail job = JobBuilder
                .newJob(jobConfig.getJobClass())
                .withIdentity(identity)
                .usingJobData(jobConfig.getJobDataMap())
                .build();

        try{
            scheduler.scheduleJob(job, jobConfig.getTrigger());
        }catch (org.quartz.SchedulerException e){
            log.error("SCHEDULER ERROR : " + e.getMessage());
            throw new SchedulerException(BaseResponseStatus.SCHEDULER_ERROR);
        }
    }

    @Override
    public void deleteIfExist(JobKey jobKey) {
        try {
            if(scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (org.quartz.SchedulerException e) {
            log.error("SCHEDULER ERROR : " + e.getMessage());
            throw new SchedulerException(BaseResponseStatus.SCHEDULER_ERROR);
        }
    }
}
