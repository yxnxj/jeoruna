package com.prography1.eruna;

import com.prography1.eruna.domain.entity.Alarm;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.N;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import reactor.util.annotation.NonNull;

public class AlarmStepExecutionListener implements StepExecutionListener {
    private static long before;

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        before = System.currentTimeMillis();
        System.out.println("----------------------------------");
        System.out.println("before : " + before);
        System.out.println("----------------------------------");
        StepExecutionListener.super.beforeStep(stepExecution);
    }


    static class WriterExecutionListener implements ItemWriteListener<Alarm>{
        long after;

        @Override
        public void beforeWrite(@NonNull Chunk<? extends Alarm> items) {
            ItemWriteListener.super.beforeWrite(items);

            after = System.currentTimeMillis();

            System.out.println("----------------------------------");
            System.out.println("after : " + after);
            System.out.println("Take time: " + (after - before) + "ms");
            System.out.println("----------------------------------");

        }
    }
}

