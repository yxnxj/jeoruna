package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class NoOpItemWriter implements ItemWriter<Alarm> {

//    private final SchedulerContext schedulerContext;
    private final Scheduler scheduler;
    @Override
    public void write(Chunk<? extends Alarm> chunk) throws Exception {
        List<? extends Alarm> alarms = chunk.getItems();
        scheduler.getContext().put("alarms", alarms);
//        chunk.getItems();
    }

}
