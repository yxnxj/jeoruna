package com.prography1.eruna.config;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.service.AlarmItemProcessor;
import com.prography1.eruna.service.DayOfWeekRowMapper;
import com.prography1.eruna.service.NoOpItemWriter;
import lombok.RequiredArgsConstructor;
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
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class BatchConfig{
    private final DataSource dataSource;

    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    @Bean
    public JdbcCursorItemReader<DayOfWeek> reader(AlarmRepository alarmRepository) {
        JdbcCursorItemReader<DayOfWeek> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("select alarm_id, day from day_of_week");
        reader.setRowMapper(new DayOfWeekRowMapper(alarmRepository));
//        reader.setMaxRows(10);
//        reader.setFetchSize(10);
        reader.setQueryTimeout(10000);
        return reader;
    }

    @Bean
    public Job readAlarmsJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("readAlarmsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
//                .listener(listener)
                .flow(step)
                .end()
                .build();
    }
    @Bean
    AlarmItemProcessor alarmItemProcessor(AlarmRepository alarmRepository) {
        return new AlarmItemProcessor(alarmRepository);
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager, AlarmRepository alarmRepository) {
        return new StepBuilder("step", jobRepository)
                .<DayOfWeek, Alarm> chunk(10, transactionManager)
                .reader(reader(alarmRepository))
                .writer(new NoOpItemWriter())
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
