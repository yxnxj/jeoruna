package com.prography1.eruna.util.scheduler;

import com.prography1.eruna.util.scheduler.job.JobConfig;
import com.prography1.eruna.util.scheduler.job.JobWithDataConfig;

interface ScheduleCreator{
    void createSchedule(JobConfig jobConfig, String identity) ;

    void createSchedule(JobWithDataConfig jobConfig, String identity);
}
