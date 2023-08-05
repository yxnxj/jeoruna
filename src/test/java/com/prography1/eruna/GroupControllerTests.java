package com.prography1.eruna;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.entity.Wakeup;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.domain.enums.Role;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.response.BaseResponse;
import com.prography1.eruna.service.GroupService;
import com.prography1.eruna.util.SendFcmJob;
import com.prography1.eruna.web.GroupReqDto;
import com.prography1.eruna.web.GroupResDto;
import com.prography1.eruna.web.UserResDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@EnableAsync
public class GroupControllerTests {

    //region field
    @Autowired
    Scheduler scheduler;

    @Autowired
    SendFcmJob sendFcmJob;

    @Autowired
    GroupService groupService;

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
    ObjectMapper objectMapper;
    MockMvc mvc;
    //endregion

    @Test
    @Transactional
    void createGroup() throws Exception {
        //given
        List<Week> days = new ArrayList<>();
        String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        Week week = Week.valueOf(day);
        days.add(week);
        LocalTime localTime = LocalTime.now();

        GroupReqDto.AlarmInfo alarmInfo = new GroupReqDto.AlarmInfo(AlarmSound.ALARM_SIU.toString(), localTime.getHour(), localTime.getMinute() + 3, days);

        User user = User.builder()
                .role(Role.USER)
                .uuid(UUID.randomUUID().toString())
                .fcmToken(UUID.randomUUID().toString())
                .build();
        userRepository.save(user);
        GroupReqDto.CreateGroup createGroup = new GroupReqDto.CreateGroup(user.getUuid(), "host", "01000000000", alarmInfo);

        //when
        String url = "/group";
        MvcResult mvcResult = mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createGroup)))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        Map<String, Object> dataString = JsonPath.parse(response).read("$.result");
        String code = (String) dataString.get("groupCode");
        Integer groupId = (Integer) dataString.get("groupId");
        Optional<Groups> group = groupRepository.findByCode(code);

        //then
        Assertions.assertTrue(group.isPresent());
        Assertions.assertEquals(group.get().getId(), groupId.longValue());
//        return new GroupResDto.CreatedGroup(groupId.longValue(), code);
    }

    String createGroupService(int delayedMinute) {
        //given
        List<Week> days = new ArrayList<>();
        String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        Week week = Week.valueOf(day);
        days.add(week);
        LocalTime localTime = LocalTime.now().plusMinutes(delayedMinute);

        GroupReqDto.AlarmInfo alarmInfo = new GroupReqDto.AlarmInfo(AlarmSound.ALARM_SIU.toString(), localTime.getHour(), localTime.getMinute(), days);

        User user = User.builder()
                .role(Role.USER)
                .uuid(UUID.randomUUID().toString())
                .fcmToken(UUID.randomUUID().toString())
                .build();
        userRepository.save(user);
        GroupReqDto.CreateGroup createGroup = new GroupReqDto.CreateGroup(user.getUuid(), "host", "01000000000", alarmInfo);

        GroupResDto.CreatedGroup createdGroup = groupService.createGroup(createGroup);

        return createdGroup.getGroupCode();
    }

    List<GroupUser> createGroupUsers(int delayedMinute) {
        int maxGroupUserCount = 4;
        String groupCode = createGroupService(delayedMinute);
        for (int i = 1; i < maxGroupUserCount; i++) {
            User user = User.builder()
                    .role(Role.USER)
                    .uuid(UUID.randomUUID().toString())
                    .fcmToken(UUID.randomUUID().toString())
                    .build();
            userRepository.save(user);

            groupService.joinGroupUser(groupCode, user.getUuid(), "nickname" + i, "01000000000");
        }

        return groupUserRepository.findByGroupsForScheduler(groupRepository.findByCode(groupCode).get());
    }


    @Test
    void allWakeup() throws Exception {
        //given
        int delayedMinute = 1;
        scheduler.start();

        List<GroupUser> groupUsers = createGroupUsers(delayedMinute);
        String url = "/group/wake-up/{groupId}/{uuid}";

        Groups group = groupUsers.get(0).getGroups();

        //when
        Thread.sleep(delayedMinute * 60 * 1000 + 60 * 1000);
        //그룹 구성원 전체 기상 post
        Assertions.assertTrue(wakeUpCacheRepository.isCachedGroupId(group.getId()));
        for (GroupUser groupUser : groupUsers) {
            MvcResult mvcResult = mvc.perform(post(url, groupUser.getGroups().getId(), groupUser.getUser().getUuid()))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = mvcResult.getResponse().getContentAsString();
            List<UserResDto.WakeupDto> dtos = toWakeupDtoList(JsonPath.parse(response).read("$.result").toString());

//                String body = toJson(dataString.get())
            for (UserResDto.WakeupDto dto : dtos) {
                if (dto.getUuid().equals(groupUser.getUser().getUuid())){
                    Assertions.assertTrue(userRepository.existsByUuid(dto.getUuid()));
                    Assertions.assertTrue(dto.getWakeup());
                }
            }
        }
        // then
        //그룹 구성원 모두 기상 시 캐싱 데이터 지워졌는지
        Assertions.assertFalse(wakeUpCacheRepository.isCachedGroupId(group.getId()));
        /**
         * FCM Token이 유효하지 않아 quartz job이 중단되고, db에 저장되지 않는다.
         */
//            for (GroupUser groupUser : groupUsers) {
//                Optional<Wakeup> wakeup = wakeupRepository.findByUser(groupUser.getUser());
//                //기상 정보 db에 잘 저장됐는지
//                Assertions.assertTrue(wakeup.isPresent());
//                Assertions.assertTrue(wakeup.get().getWakeupCheck());
//            }
//            boolean terminated = asyncTaskExecutor.getThreadPoolExecutor().awaitTermination(delayMinute * 60 + 20 * 60, TimeUnit.SECONDS); //유효한 fcmtoken일때
    }


    @BeforeEach
    public void setup(@Autowired Job job) throws SchedulerException {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        scheduler.clear();
    }

    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

    private List<UserResDto.WakeupDto> toWakeupDtoList(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<List<UserResDto.WakeupDto>>() {
        });
    }
}
