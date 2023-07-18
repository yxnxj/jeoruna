package com.prography1.eruna.batch.scheduler;


import com.prography1.eruna.ErunaApplication;
import com.prography1.eruna.config.FCMConfig;
import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.domain.enums.Role;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.UserService;
import com.prography1.eruna.util.SendFcmJob;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


@SpringBatchTest
@SpringBootTest(classes = ErunaApplication.class
//        , properties = "spring.main.allow-bean-definition-overriding=true"
)
@ActiveProfiles("local")
//@EnableAutoConfiguration
//@SpringJUnitConfig(TestBatchConfig.class)
//@Import({
//        FCMConfig.class})
//@EnableJpaRepositories("com.prography1.eruna.domain.repository")
//@DataJpaTest
//@EnableJpaAuditing
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

    @Autowired
    Scheduler scheduler;

    @Autowired
    SendFcmJob sendFcmJob;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    DayOfWeekRepository dayOfWeekRepository;

    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    GroupUserRepository groupUserRepository;

    @BeforeEach
    public void setup(@Autowired Job job) {
        alarmRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        this.jobLauncherTestUtils.setJobRepository(jobRepository);
        this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
        this.jobLauncherTestUtils.setJob(job); // this is optional if the job is unique
        this.jobRepositoryTestUtils.removeJobExecutions();
    }

    public void createAlarmRecordsForTest(int delayMinute){
        int size = 3;
        for (int i = 0; i< size; i++){
            User user = User.builder()
                    .role(Role.USER)
                    .uuid(UUID.randomUUID().toString())
                    .fcmToken(UUID.randomUUID().toString())
                    .build();

            Groups group = Groups.create(userRepository.save(user));

            Alarm alarm = Alarm.builder()
                    .alarmTime(LocalTime.now().plusMinutes(delayMinute))
                    .alarmSound(AlarmSound.ALARM_SIU)
                    .finishDate(LocalDate.now())
                    .startDate(LocalDate.now())
                    .groups(groupRepository.save(group))
                    .build();

            GroupUser groupUser = GroupUser.builder().user(user).groups(group).nickname("nickname")
                    .phoneNum("01000000000")
                    .groupUserId(GroupUser.GroupUserId.builder().groupId(group.getId()).userId(user.getId()).build())
                    .build();
            groupUserRepository.save(groupUser);
            String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
            Week week = Week.valueOf(day);
            DayOfWeek.DayOfWeekId dayOfWeekId = new DayOfWeek.DayOfWeekId(alarm.getId(), week);
            DayOfWeek dayOfWeek = new DayOfWeek(dayOfWeekId, alarmRepository.save(alarm));

            dayOfWeekRepository.save(dayOfWeek);
        }
    }

    @Test
    public void testMyJob() throws Exception {
        // given
        JobParameters jobParameters = this.jobLauncherTestUtils.getUniqueJobParameters();
        int delayMinute = 1;
        createAlarmRecordsForTest(delayMinute);


        // when
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);
        String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        List<Alarm> alarms = dayOfWeekRepository.findAllAlarmsByDay(Week.valueOf(day));


        // then

        List<User> users = new ArrayList<>();
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        List<JobKey> keys = scheduler.getJobKeys(GroupMatcher.anyGroup()).stream().toList();

        for (Alarm alarm : alarms){
            Groups group = groupRepository.findByAlarm(alarm).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_GROUP));
            List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);
            for(GroupUser groupUser : groupUsers){
                users.add(groupUser.getUser());
                Assertions.assertTrue(scheduler.checkExists(JobKey.jobKey(groupUser.getUser().getUuid())));
            }
        }

        List<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyGroup()).stream().toList();
        scheduler.start();
        Thread.sleep(   delayMinute * 60 * 1000 + 1000);

        for(TriggerKey triggerKey : triggerKeys){
            /**
             * FCM Token 이 Random UUID로 생성되는 유효하지 않은 토큰이므로 JOB이 삭제된다.
             * 삭제 됐으므로 Trigger의 상태는 NONE이다.
             */
            Assertions.assertEquals(Trigger.TriggerState.NONE, scheduler.getTriggerState(triggerKey));
        }
    }

}
