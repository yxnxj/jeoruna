package com.prography1.eruna.batch.scheduler;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.service.AlarmService;
import com.prography1.eruna.util.AlarmsItemWriter;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;


//@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = {"com.prography1.eruna"}
        , excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Configuration.class})}
)
//@Import(CustomConfig.class)
public class TestBatchConfig {

//    @PersistenceContext
//    private EntityManager entityManager;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    Scheduler scheduler;

    private static final Logger logger = LoggerFactory.getLogger(com.prography1.eruna.config.BatchConfig.class);

    @Bean
    public JpaPagingItemReader<DayOfWeek> jpaPagingItemReader() {
        LocalDate localDate = LocalDate.now();
        String today = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        HashMap<String, Object> paramValues = new HashMap<>();
        String query =
                "SELECT dayOfWeek.alarm From DayOfWeek dayOfWeek WHERE dayOfWeek.dayOfWeekId.day = :today ";
        paramValues.put("today", Week.valueOf(today));
        logger.info("Day: " + today);


        return new JpaPagingItemReaderBuilder<DayOfWeek>()
                .name("alarmReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(2)
                .queryString(query)
//                .queryString("select d from DayOfWeek d where d.dayOfWeekId.day = :today")
                .parameterValues(paramValues)
                .build();
    }

    @Bean
    public Job readAlarmsJob(JobRepository jobRepository,  @Qualifier("readAlarmsStep") Step step) {
        return new JobBuilder("readAlarmsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    public ItemWriter<Alarm> writer(GroupRepository groupRepository, AlarmService alarmService) {
        return new AlarmsItemWriter(groupRepository, alarmService);
    }

    @Bean
    AlarmItemProcessor alarmItemProcessor(AlarmRepository alarmRepository) {
        return new AlarmItemProcessor(alarmRepository);
    }

    @Bean
    public Step readAlarmsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, GroupRepository groupRepository, AlarmService alarmService) {
        return new StepBuilder("step", jobRepository)
                .<DayOfWeek, Alarm>chunk(10, transactionManager)
//                .reader(reader(alarmRepository))
                .reader(jpaPagingItemReader())
                .writer(writer(groupRepository, alarmService))
//                .processor(alarmItemProcessor(alarmRepository))
                .allowStartIfComplete(true)
                .build();
    }

//    @Bean
//    public Scheduler scheduler() throws SchedulerException {
//        return StdSchedulerFactory.getDefaultScheduler();
//    }
}
