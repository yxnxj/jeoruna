package com.prography1.eruna.util.scheduler;

import com.prography1.eruna.util.scheduler.job.JobConfig;

interface ScheduleCreator{
    void createSchedule(JobConfig jobConfig, String identity) ;
}
