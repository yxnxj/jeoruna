package com.prography1.eruna.config;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.util.AlarmItemProcessor;
import com.prography1.eruna.util.DayOfWeekRowMapper;
import com.prography1.eruna.util.JobCompletionNotificationListener;
import com.prography1.eruna.util.NoOpItemWriter;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class BatchConfig{
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);

//    @Bean
    public JdbcCursorItemReader<DayOfWeek> reader(AlarmRepository alarmRepository) {
        JdbcCursorItemReader<DayOfWeek> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("select alarm_id, day from day_of_week");
        reader.setRowMapper(new DayOfWeekRowMapper(alarmRepository));
        reader.setMaxRows(10);
        reader.setFetchSize(10);
        reader.setQueryTimeout(10000);
        reader.close();
        return reader;
    }

    @Bean
    public JpaPagingItemReader<DayOfWeek> jpaPagingItemReader(){
        LocalDate localDate = LocalDate.now();
        String today = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        HashMap<String, Object> paramValues = new HashMap<>();
        paramValues.put("today", Week.valueOf(today));

        return new JpaPagingItemReaderBuilder<DayOfWeek>()
                .name("alarmReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(2)
                .queryString("select d from DayOfWeek d where d.dayOfWeekId.day = :today")
                .parameterValues(paramValues)
                .build();
    }

    @Bean
    public Job readAlarmsJob(JobRepository jobRepository, JobCompletionNotificationListener listener,Step step) {
        return new JobBuilder("readAlarmsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }
    @Bean
    AlarmItemProcessor alarmItemProcessor(AlarmRepository alarmRepository) {
        return new AlarmItemProcessor(alarmRepository);
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager, AlarmRepository alarmRepository, Scheduler scheduler) {
        return new StepBuilder("step", jobRepository)
                .<DayOfWeek, Alarm> chunk(10, transactionManager)
//                .reader(reader(alarmRepository))
                .reader(jpaPagingItemReader())
                .writer(new NoOpItemWriter(scheduler))
                .processor(alarmItemProcessor(alarmRepository))
                .allowStartIfComplete(true)
                .build();
    }

//    private final JobRepository jobRepository;
    private final ApplicationContext applicationContext;
    private final JobLauncher jobLauncher;



    @Scheduled(cron = "0 0 0 * * *")
//    @Scheduled(fixedRate = 1000)
    public void launchJob() throws Exception {
        Date date = new Date();

        JobExecution jobExecution = jobLauncher.run(
                (Job)applicationContext.getBean("readAlarmsJob")
                ,new JobParametersBuilder().addDate("launchDate", date).toJobParameters()
        );
//            batchRunCounter.incrementAndGet();
        logger.debug("Batch job ends with status as " + jobExecution.getStatus());
        logger.debug("scheduler ends ");
    }
}
