package com.prography1.eruna.util.scheduler;

import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.util.scheduler.job.JobConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class ScheduleManager implements ScheduleCreator{

    private final Scheduler scheduler;
    @Override
    public void createSchedule(JobConfig jobConfig, String identity) {
        JobDetail job = JobBuilder
                .newJob(jobConfig.getJobClass())
                .withIdentity(identity)
                .usingJobData(jobConfig.getJobDataMap())
                .build();

        try{
            if(scheduler.checkExists(job.getKey())) {
                scheduler.deleteJob(job.getKey());
            }
            log.info("__________Schedule__________");
            log.info("identity : " + identity + ", alarm : " + jobConfig.getTrigger().getStartTime());

            scheduler.scheduleJob(job, jobConfig.getTrigger());
        }catch (SchedulerException e){
            log.error("SCHEDULER ERROR : " + e.getMessage());
            throw new BaseException(BaseResponseStatus.SCHEDULER_ERROR);
        }
    }
}
