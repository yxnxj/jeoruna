package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.exception.notfound.AlarmNotFoundException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class AlarmsItemWriter implements ItemWriter<Alarm> {

//    private final SchedulerContext schedulerContext;
//    private final Scheduler scheduler;
    private final GroupRepository groupRepository;
    private final AlarmService alarmService;
    @Override
    @SuppressWarnings("unchecked")
    public void write(Chunk<? extends Alarm> chunk) {
        List<? extends Alarm> alarms = chunk.getItems();
//        Object storedAlarms = scheduler.getContext().get("alarms");
//        if (storedAlarms != null){
//            alarms = Stream.concat(alarms.stream(),((List<Alarm>) storedAlarms).stream()
//            ).toList();
//        }
//        scheduler.getContext().put("alarms", alarms);
//        if(alarms == null) alarms = new ArrayList<>();
        for (Alarm alarm : alarms) {
            Groups group = groupRepository.findByAlarm(alarm).orElseThrow(() -> new AlarmNotFoundException(BaseResponseStatus.NOT_FOUND_ALARM, String.format("Scheduler에 작성할 alarm %d를 찾지 못했습니다.", alarm.getId())));
            alarmService.createAlarmScheduleInGroup(alarm, group);
        }
//        chunk.getItems();
    }

}
