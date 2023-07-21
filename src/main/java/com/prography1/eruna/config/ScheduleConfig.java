package com.prography1.eruna.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduleConfig {
    private final ApplicationContext applicationContext;
    private final JobLauncher jobLauncher;
    private final Scheduler scheduler;

    @Scheduled(cron = "0 0 0 * * *")
//    @Scheduled(fixedRate = 1000)
    public void launchJob() throws Exception {
        Date date = new Date();
        scheduler.clear();

        JobExecution jobExecution = jobLauncher.run(
                (Job)applicationContext.getBean("readAlarmsJob")
                ,new JobParametersBuilder().addDate("launchDate", date).toJobParameters()
        );
//            batchRunCounter.incrementAndGet();
        log.debug("Batch job ends with status as " + jobExecution.getStatus());
        log.debug("scheduler ends ");
    }
}
