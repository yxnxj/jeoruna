package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class AlarmsItemWriter implements ItemWriter<Alarm> {

//    private final SchedulerContext schedulerContext;
    private final Scheduler scheduler;
    @Override
    @SuppressWarnings("unchecked")
    public void write(Chunk<? extends Alarm> chunk) throws Exception {
        List<? extends Alarm> alarms = chunk.getItems();
        Object storedAlarms = scheduler.getContext().get("alarms");
        if (storedAlarms != null){
            alarms = Stream.concat(alarms.stream(),((List<Alarm>) storedAlarms).stream()
            ).toList();
        }
        scheduler.getContext().put("alarms", alarms);
//        chunk.getItems();
    }

}
