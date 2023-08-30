package com.prography1.eruna.util.scheduler;

import com.prography1.eruna.response.BaseException;
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
        }catch (SchedulerException e){
            log.error("SCHEDULER ERROR : " + e.getMessage());
            throw new BaseException(BaseResponseStatus.SCHEDULER_ERROR);
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
        }catch (SchedulerException e){
            log.error("SCHEDULER ERROR : " + e.getMessage());
            throw new BaseException(BaseResponseStatus.SCHEDULER_ERROR);
        }
    }

    @Override
    public void deleteIfExist(JobKey jobKey) {
        try {
            if(scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            log.error("SCHEDULER ERROR : " + e.getMessage());
            throw new BaseException(BaseResponseStatus.SCHEDULER_ERROR);
        }
    }
}
