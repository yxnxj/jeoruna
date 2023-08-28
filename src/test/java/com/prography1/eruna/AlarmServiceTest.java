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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;


//@SpringBootTest
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AlarmServiceTest {
    @Autowired
    public UserRepository userRepository;

    @Autowired
    public GroupRepository groupRepository;

    @Autowired
    public AlarmRepository alarmRepository;

    @Autowired
    public DayOfWeekRepository dayOfWeekRepository;

    @Autowired
    public TestEntityManager testEntityManager;


//    @BeforeEach
//    @Transactional
    public void createAlarmRecordsForTest(){
        int size = 10000;
        for (int i = 0; i< size; i++){
            User user = User.builder()
                    .role(Role.USER)
                    .uuid(UUID.randomUUID().toString())
                    .fcmToken(UUID.randomUUID().toString())
                    .build();

            Groups group = Groups.create(userRepository.save(user));
            Week[] weeks = Week.values();
            Week week = weeks[new Random().nextInt(weeks.length)];
            Alarm alarm = Alarm.builder()
                    .alarmTime(LocalTime.now().plusMinutes(5))
                    .alarmSound(AlarmSound.ALARM_SIU)
                    .finishDate(LocalDate.now())
                    .startDate(LocalDate.now())
                    .groups(groupRepository.save(group))
                    .build();
//            String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
//            Week week = Week.valueOf(day);
            DayOfWeek.DayOfWeekId dayOfWeekId = new DayOfWeek.DayOfWeekId(alarm.getId(), week);
            DayOfWeek dayOfWeek = new DayOfWeek(dayOfWeekId, alarmRepository.save(alarm));

            dayOfWeekRepository.save(dayOfWeek);
//            List<DayOfWeek> days = new ArrayList<>();
//            days.add(dayOfWeek);
//            alarm.setWeekList(days);
        }

    }

    /**
     * Result
     *  - Take time: 776ms
     *  - found size : 1426
     */
    @Test
//    @Transactional
//    @Rollback(false)
    public void measurePerformanceGettingTodayAlarms(){
        String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        long start = System.currentTimeMillis();

//        List<Alarm> alarms = alarmRepository.findByWeekList_DayOfWeekId_Day(Week.valueOf(day));
        List<Alarm> alarms = dayOfWeekRepository.findAllAlarmsByDay(Week.valueOf(day));

        long end = System.currentTimeMillis();


        for (Alarm alarm : alarms){
            testEntityManager.refresh(testEntityManager.merge(alarm));
            List<DayOfWeek> weekList = alarm.getWeekList();
            for (DayOfWeek d : weekList){
                Assertions.assertEquals(d.getDayOfWeekId().getDay(), Week.valueOf(day));
            }
        }
        System.out.println("----------------------------------");
        System.out.println("Take time: " + (end - start) + "ms");
        System.out.println("found size : " + alarms.size());
        System.out.println("----------------------------------");
        Alarm randomAlarm = alarms.get(alarms.size()-1);

        Alarm refreshAlarm = alarmRepository.findById(randomAlarm.getId()).get();
        List<DayOfWeek> days  =dayOfWeekRepository.findAllByAlarm(randomAlarm);

    }
}
