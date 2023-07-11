package com.prography1.eruna;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

@Configuration
//@RequiredArgsConstructor
class DataSourceConfig {


    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mysql://localhost:3306/eruna?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&&createDatabaseIfNotExist=true");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("jo1219");
        return dataSourceBuilder.build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[] { "\\com.prography1.eruna.domain" });

        Properties properties = new Properties();
        properties.setProperty("show-sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
//        properties.setProperty("hibernate.implicit_naming_strategy" , "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
//        properties.setProperty("hibernate.physical_naming_strategy" , "com.prography1.eruna.CustomPhysicalNamingStrategy");
//        properties.setProperty("hibernate.naming.strategy" , "org.hibernate.cfg.ImprovedNamingStrategy ");
        em.setJpaProperties(properties);

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.afterPropertiesSet();

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);

        return transactionManager;
    }

}

@Configuration
@Import(DataSourceConfig.class)
@EnableBatchProcessing
class ReadAlarmBatchConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(com.prography1.eruna.config.BatchConfig.class);

    @Bean
    public JpaPagingItemReader<DayOfWeek> jpaPagingItemReader(EntityManagerFactory entityManagerFactory) {
        LocalDate localDate = LocalDate.now();
        String today = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        HashMap<String, Object> paramValues = new HashMap<>();
        String query =
                "SELECT alarm FROM Alarm alarm";
//                        + "WHERE EXISTS (SELECT d FROM alarm.weekList d where d.dayOfWeekId.day = :today)";
//        paramValues.put("today", Week.valueOf(today));
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



    @Bean
    public Step readAlarmsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, EntityManagerFactory entityManagerFactory) throws Exception {
        return new StepBuilder("step", jobRepository)
                .<DayOfWeek, Alarm>chunk(10, transactionManager)
                .reader(jpaPagingItemReader(entityManagerFactory))
                .writer(writer())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public FlatFileItemWriter<Alarm> writer() throws Exception {
        WritableResource outputResource = new FileSystemResource("output/outputData");
        //Create writer instance
        FlatFileItemWriter<Alarm> writer = new FlatFileItemWriter<>();

        //Set output file location
        writer.setResource(outputResource);

        //All job repetitions should "append" to same output file
        writer.setAppendAllowed(true);

        //Name field values sequence based on object properties
        LineAggregator<Alarm> aggregator = item -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule()).writeValueAsString("Alarm");
                return mapper.writeValueAsString(item);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return "";
        };
        writer.setLineAggregator(aggregator);
        writer.afterPropertiesSet();
        return writer;
    }


    @Bean
    public JobRepositoryFactoryBean jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDataSource(dataSource);
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        return jobRepositoryFactoryBean;
    }


}

@SpringBatchTest
@SpringJUnitConfig(ReadAlarmBatchConfiguration.class)
//@SpringBootTest
public class ReadAlarmBatchJobTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;


    @BeforeEach
    public void setup(@Autowired Job job) {
        this.jobLauncherTestUtils.setJobRepository(jobRepository);
        this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
        this.jobLauncherTestUtils.setJob(job); // this is optional if the job is unique
        this.jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    public void testMyJob() throws Exception {
        // given
        JobParameters jobParameters = this.jobLauncherTestUtils.getUniqueJobParameters();

        // when
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);

        // then
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }

}

