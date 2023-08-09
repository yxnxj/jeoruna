package com.prography1.eruna;

import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.domain.enums.Role;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.service.AlarmService;
import com.prography1.eruna.service.WakeupService;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Stream;

@SpringBootTest
public class WakeupServiceTests {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;
    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    DayOfWeekRepository dayOfWeekRepository;
    @Autowired
    GroupUserRepository groupUserRepository;

    @Autowired
    WakeupService wakeupService;
    @Autowired
    AlarmService alarmService;

    @Autowired
    Scheduler scheduler;

    public List<Alarm> createAlarmRecordsForTest(int size, int delayMinute) {
        List<Alarm> alarms = new ArrayList<>();
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

            groupUserRepository.save(hostGroupUser);


            String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
            Week week = Week.valueOf(day);
            DayOfWeek.DayOfWeekId dayOfWeekId = new DayOfWeek.DayOfWeekId(alarm.getId(), week);
            DayOfWeek dayOfWeek = new DayOfWeek(dayOfWeekId, alarmRepository.save(alarm));

            dayOfWeekRepository.save(dayOfWeek);
            alarms.add(alarm);

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
                groupUserRepository.save(groupUser);
            }
        }

        return alarms;
    }

    @Test
    @Transactional
    void wakeupWithCache() throws SchedulerException, InterruptedException {
        //given
        int size = 100;
        int delayMinutes = 1;
        List<Alarm> alarms =  createAlarmRecordsForTest(size, delayMinutes);
        List<GroupUser> groupUsers = new ArrayList<>();
        //when
        for(Alarm alarm : alarms){
            alarmService.createAlarmScheduleInGroup(alarm);
            groupUsers.addAll(groupUserRepository.findByGroupsForScheduler(alarm.getGroups()));
        }
        scheduler.start();
        Thread.sleep(2* delayMinutes * 60 * 1000);
        long start = System.currentTimeMillis();
        for (GroupUser groupUser : groupUsers){
            wakeupService.updateWakeupInfo(groupUser.getGroups().getId(), groupUser.getUser().getUuid());
        }
        long end = System.currentTimeMillis();
        //then
        System.out.println(end - start );

    }
}
