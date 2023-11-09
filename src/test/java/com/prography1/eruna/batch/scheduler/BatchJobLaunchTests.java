package com.prography1.eruna.batch.scheduler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.domain.enums.Role;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.exception.notfound.GroupNotFoundException;
import com.prography1.eruna.util.scheduler.job.SendFcmJob;
import com.prography1.eruna.web.UserResDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

import static com.prography1.eruna.response.BaseResponseStatus.NOT_FOUND_GROUP;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBatchTest
@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class BatchJobLaunchTests {

    //region field
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

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
    @Autowired
    WakeupRepository wakeupRepository;

    @Autowired
    WakeUpCacheRepository wakeUpCacheRepository;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;
//endregion
    @BeforeEach
    public void setup(@Autowired Job job) throws SchedulerException {
        this.jobLauncherTestUtils.setJob(job); // this is optional if the job is unique
        this.jobRepositoryTestUtils.removeJobExecutions();
        scheduler.clear();
    }

    @Test
    void clearDB() {
        wakeupRepository.deleteAll();
        groupUserRepository.deleteAll();
        groupRepository.deleteAll();

        userRepository.deleteAll();
        alarmRepository.deleteAll();
    }

    void launchJob() throws Exception{
        JobParameters jobParameters = this.jobLauncherTestUtils.getUniqueJobParameters();
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);
    }

    //    @Async
    void startSchedule(int delayMinute) {
        try {
            scheduler.start();
            Thread.sleep(delayMinute * 60 * 1000);

        } catch (SchedulerException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<GroupUser> createAlarmRecordsForTest(int size, int delayMinute) {
        List<GroupUser> groupUsers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
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

            GroupUser hostGroupUser = GroupUser.builder().user(user).groups(group).nickname("host")
                    .phoneNum("01000000000")
                    .groupUserId(GroupUser.GroupUserId.builder().groupId(group.getId()).userId(user.getId()).build())
                    .build();
            groupUsers.add(hostGroupUser);

            groupUserRepository.save(hostGroupUser);


            String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
            Week week = Week.valueOf(day);
            DayOfWeek.DayOfWeekId dayOfWeekId = new DayOfWeek.DayOfWeekId(alarm.getId(), week);
            DayOfWeek dayOfWeek = new DayOfWeek(dayOfWeekId, alarmRepository.save(alarm));

            dayOfWeekRepository.save(dayOfWeek);

            for (int j = 0; j < 3; j++) {

                User newUser = User.builder()
                        .role(Role.USER)
                        .uuid(UUID.randomUUID().toString())
                        .fcmToken(UUID.randomUUID().toString())
                        .build();
                userRepository.save(newUser);
                GroupUser groupUser = GroupUser.builder().user(newUser).groups(group).nickname("nickname" + j)
                        .phoneNum("01000000000")
                        .groupUserId(GroupUser.GroupUserId.builder().groupId(group.getId()).userId(newUser.getId()).build())
                        .build();
                groupUsers.add(groupUser);
                groupUserRepository.save(groupUser);
            }
        }

        return groupUsers;
    }

    @Test
    public void isAlarmRegisteredInSchedule() throws Exception {
        // given
        clearDB();

        JobParameters jobParameters = this.jobLauncherTestUtils.getUniqueJobParameters();
        int delayMinute = 3;
        int size = 2500;
        int groupUserSize = 4;
        createAlarmRecordsForTest(size, delayMinute);


        // when
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);
        String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        List<Alarm> alarms = dayOfWeekRepository.findAllAlarmsByDay(Week.valueOf(day));


        // then

        List<User> users = new ArrayList<>();
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        List<JobKey> keys = scheduler.getJobKeys(GroupMatcher.anyGroup()).stream().toList();
        Assertions.assertEquals(size * groupUserSize, keys.size());
        for (Alarm alarm : alarms) {
            Groups group = groupRepository.findByAlarm(alarm).orElseThrow(()-> new GroupNotFoundException(NOT_FOUND_GROUP, String.format("%d alarm을 갖는 group을 찾을 수 없습니다.", alarm.getId())));
            List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);
            for (GroupUser groupUser : groupUsers) {
                users.add(groupUser.getUser());
                Assertions.assertTrue(scheduler.checkExists(JobKey.jobKey(groupUser.getUser().getUuid())));
            }
        }

        scheduler.start();
        Thread.sleep(2 * delayMinute * 60 * 1000 );

        List<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyGroup()).stream().toList();
        for (TriggerKey triggerKey : triggerKeys) {
            /**
             * FCM Token 이 Random UUID로 생성되는 유효하지 않은 토큰이므로 JOB이 삭제된다.
             * 삭제 됐으므로 Trigger의 상태는 NONE이다.
             *
             * FcmToken 유효할 때 Job State : NORMAL
             * FcmToken 유효하지 않을 때 Job State : NONE
             */
            Assertions.assertEquals(Trigger.TriggerState.NONE, scheduler.getTriggerState(triggerKey));
        }
    }


    /**
     * 실제 FCM job이 반복 실행되는지 확인하려면 AlarmService 단에서 IsValidFcmToken 검증 코드를 주석처리 해야 한다.
     *
     * @throws Exception
     */

    @Test
    void wakeUpRequestTest() throws Exception {

        //given
        int delayMinute = 3;
        int size = 2500;

        clearDB();
        List<GroupUser> groupUsers = createAlarmRecordsForTest(size, delayMinute);

        //when
       launchJob();
//        startSchedule(delayMinute);
//        scheduler.start();
        Thread.sleep(2 * delayMinute * 60 * 1000);

        String url = "/group/wake-up/{groupId}/{uuid}";
        List<Groups> groups = groupRepository.findAll();

        //quartz job 후 기상 정보 캐싱 됐는지
        for (Groups group : groups) {
            Assertions.assertTrue(wakeUpCacheRepository.isCachedGroupId(group.getId()));
        }

        //그룹 구성원 전체 기상 post
        for (GroupUser groupUser : groupUsers) {
            MvcResult mvcResult = mvc.perform(post(url, groupUser.getGroups().getId(), groupUser.getUser().getUuid()))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = mvcResult.getResponse().getContentAsString();
            List<UserResDto.WakeupDto> dtos = toWakeupDtoList(JsonPath.parse(response).read("$.result").toString());


            // then
            for (UserResDto.WakeupDto dto : dtos) {
                //기상 정보가 유저의 캐싱 데이터에 잘 반영 됐는지
                if (dto.getUuid().equals(groupUser.getUser().getUuid())) {
                    Assertions.assertTrue(userRepository.existsByUuid(dto.getUuid()));
                    Assertions.assertTrue(dto.getWakeup());
                }
            }
        }
        for (Groups group : groups) {
            //그룹 구성원 모두 기상 시 캐싱 데이터 지워졌는지
            Assertions.assertFalse(wakeUpCacheRepository.isCachedGroupId(group.getId()));
        }
        for (GroupUser groupUser : groupUsers) {
            Optional<Wakeup> wakeup = wakeupRepository.findByUser(groupUser.getUser());
            //기상 정보 db에 잘 저장됐는지
            Assertions.assertTrue(wakeup.isPresent());
            Assertions.assertTrue(wakeup.get().getWakeupCheck());
        }

    }
    private List<UserResDto.WakeupDto> toWakeupDtoList(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<List<UserResDto.WakeupDto>>() {
        });
    }
}
