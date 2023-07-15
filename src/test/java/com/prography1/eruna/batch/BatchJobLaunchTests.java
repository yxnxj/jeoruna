package com.prography1.eruna.batch;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.prography1.eruna.config.BatchConfig;
import com.prography1.eruna.config.FCMConfig;
import com.prography1.eruna.config.RedisConfig;
import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.service.AlarmService;
import com.prography1.eruna.service.UserService;
import com.prography1.eruna.util.AlarmItemProcessor;
import com.prography1.eruna.util.AlarmsItemWriter;
import com.prography1.eruna.util.DayOfWeekRowMapper;
import com.prography1.eruna.util.JobCompletionNotificationListener;
import com.prography1.eruna.web.UserResDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = {"com.prography1.eruna"}
        , excludeFilters={@ComponentScan.Filter(type= FilterType.ANNOTATION, classes = {Configuration.class})}
)
class TestBatchConfig{
    @Autowired
    EntityManagerFactory entityManagerFactory;

    private static final Logger logger = LoggerFactory.getLogger(com.prography1.eruna.config.BatchConfig.class);

    @Bean
    public JpaPagingItemReader<DayOfWeek> jpaPagingItemReader(){
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
    public Job readAlarmsJob(JobRepository jobRepository, JobCompletionNotificationListener listener, @Qualifier("readAlarmsStep") Step step) {
        return new JobBuilder("readAlarmsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(step)
                .build();
    }

    public ItemWriter<Alarm> writer(Scheduler scheduler){
        return new AlarmsItemWriter(scheduler);
    }
    @Bean
    AlarmItemProcessor alarmItemProcessor(AlarmRepository alarmRepository) {
        return new AlarmItemProcessor(alarmRepository);
    }

    @Bean
    public Step readAlarmsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, AlarmRepository alarmRepository, Scheduler scheduler) {
        return new StepBuilder("step", jobRepository)
                .<DayOfWeek, Alarm> chunk(10, transactionManager)
//                .reader(reader(alarmRepository))
                .reader(jpaPagingItemReader())
                .writer(writer(scheduler))
//                .processor(alarmItemProcessor(alarmRepository))
                .allowStartIfComplete(true)
                .build();
    }

}

@Configuration
class TestRedisConfig {

    private String redisHost;

    private Integer redisPort;

    public TestRedisConfig(String redisHost, Integer redisPort) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
    }

    public TestRedisConfig() {
        this.redisHost = "127.0.0.1";
        this.redisPort = 6379;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, UserResDto.WakeupDto> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        // Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(ItemDto.class);
        RedisTemplate<String, UserResDto.WakeupDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // redisTemplate.setValueSerializer(new StringRedisSerializer());
        // redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        return redisTemplate;
    }
}
@Configuration
//@ComponentScan(basePackages = {"com.prography1.eruna"}
//        , excludeFilters={@ComponentScan.Filter(type= FilterType.ANNOTATION, classes = {Configuration.class})}
//)
@Import(TestRedisConfig.class)
//@RequiredArgsConstructor
class CustomConfig {


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
        properties.setProperty("spring.data.redis.host", "127.0.0.1");
        properties.setProperty("spring.data.redis.port", "6379");

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

    @Bean
    public Scheduler getScheduler() throws SchedulerException {
        return StdSchedulerFactory.getDefaultScheduler();
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
@SpringJUnitConfig(TestBatchConfig.class)
@Import({CustomConfig.class, FCMConfig.class})
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BatchJobLaunchTests {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @MockBean
    GroupRepository groupRepository;

    @MockBean
    AlarmService alarmService;

    @MockBean
    AlarmRepository alarmRepository;

    @MockBean
    UserRepository userRepository;
    @MockBean
    GroupUserRepository groupUserRepository;

    @MockBean
    DayOfWeekRepository dayOfWeekRepository;

    @MockBean
    WakeupRepository wakeupRepository;

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
//    @Test
//    void save(){
//        alarmRepository.findAll();
//    }
}
