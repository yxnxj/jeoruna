package com.prography1.eruna.util.scheduler;

import org.quartz.JobKey;

interface ScheduleDeleter {
    void deleteIfExist(JobKey jobKey);
}
