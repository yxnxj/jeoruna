package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class NoOpItemWriter implements ItemWriter<Alarm> {

    @Override
    public void write(Chunk chunk) throws Exception {

    }
}
