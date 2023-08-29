package com.prography1.eruna.util.scheduler.job;

import lombok.Getter;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.Trigger;

@Getter
public class JobWithDataConfig extends JobConfig{

    private final JobDataMap jobDataMap;

    public JobWithDataConfig(Class<? extends Job> jobClass, JobDataMap jobDataMap, Trigger trigger) {
        super(jobClass, trigger);
        this.jobDataMap = jobDataMap;
    }
}
