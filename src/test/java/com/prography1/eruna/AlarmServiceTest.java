package com.prography1.eruna;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.domain.enums.Role;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.domain.repository.DayOfWeekRepository;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.domain.repository.UserRepository;
import com.prography1.eruna.service.AlarmService;
import com.prography1.eruna.web.UserResDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;


@SpringBootTest
public class AlarmServiceTest {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public GroupRepository groupRepository;

    @Autowired
    public AlarmRepository alarmRepository;

    @Autowired
    public DayOfWeekRepository dayOfWeekRepository;

    public void createAlarmRecordsForTest(){
        int size = 10000;
        for (int i = 0; i< size; i++){
            User user = User.builder()
                    .role(Role.USER)
                    .uuid(UUID.randomUUID().toString())
                    .fcmToken(UUID.randomUUID().toString())
                    .build();

            Groups group = Groups.create(userRepository.save(user));

            Alarm alarm = Alarm.builder()
                    .alarmTime(LocalTime.now().plusMinutes(size/20000))
                    .alarmSound(AlarmSound.ALARM_SIU)
                    .finishDate(LocalDate.now())
                    .startDate(LocalDate.now())
                    .groups(groupRepository.save(group))
                    .build();
            Week[] weeks = Week.values();
            Week week = weeks[new Random().nextInt(weeks.length)];
//            String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
//            Week week = Week.valueOf(day);
            DayOfWeek.DayOfWeekId dayOfWeekId = new DayOfWeek.DayOfWeekId(alarm.getId(), week);
            DayOfWeek dayOfWeek = new DayOfWeek(dayOfWeekId, alarmRepository.save(alarm));

            dayOfWeekRepository.save(dayOfWeek);
        }
    }

    @Test
    @Transactional
    public void measurePerformanceGettingTodayAlarms(){
        createAlarmRecordsForTest();
        String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        long start = System.currentTimeMillis();

        List<Alarm> alarms = alarmRepository.findAllOnDay(day);

        long end = System.currentTimeMillis();

        System.out.println("Take time: " + (end - start));
    }
}
