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
import jakarta.persistence.*;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;


//@SpringBootTest
@ExtendWith(SpringExtension.class)
@DataJpaTest
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


    @BeforeEach
//    @Transactional
    public void createAlarmRecordsForTest(){
        int size = 100;
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
                    .alarmTime(LocalTime.now().plusMinutes(size/20))
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

    @Test
    @Transactional
    public void measurePerformanceGettingTodayAlarms(){
//        createAlarmRecordsForTest();
        String day = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
        long start = System.currentTimeMillis();

//        List<Alarm> alarms = alarmRepository.findAllOnDay(day);
        List<Alarm> alarms = alarmRepository.findByWeekList_DayOfWeekId_Day(Week.valueOf(day));
        for (Alarm alarm : alarms){
            testEntityManager.refresh(testEntityManager.merge(alarm));
        }
//        boolean is = Hibernate.isInitialized(alarms);

        long end = System.currentTimeMillis();
        System.out.println("----------------------------------");
        System.out.println("Take time: " + (end - start) + "ms");
        System.out.println("found size : " + alarms.size());
        Alarm randomAlarm = alarms.get(alarms.size()-1);
//        entityManager.flush();
//        entityManager.detach(randomAlarm);
//        entityManager.clear();
//        entityManager.close();

        Alarm refreshAlarm = alarmRepository.findById(randomAlarm.getId()).get();
        List<DayOfWeek> days  =dayOfWeekRepository.findAllByAlarm(randomAlarm);

//        List<String> weekList = new ArrayList<>();
//        randomAlarm.getWeekList().forEach(week-> weekList.add(week.getDayOfWeekId().getDay().name()));
        System.out.println(randomAlarm.getWeekList());
    }
}
