package com.prography1.eruna.util.scheduler.job;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.Trigger;

@Getter
@SuperBuilder
public class JobWithDataConfig extends JobConfig{

    private final JobDataMap jobDataMap;

//    @Builder
    public JobWithDataConfig(Class<? extends Job> jobClass, JobDataMap jobDataMap, Trigger trigger) {
        super(jobClass, trigger);
        this.jobDataMap = jobDataMap;
    }
}
