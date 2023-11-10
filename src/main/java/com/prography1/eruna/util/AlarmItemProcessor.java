package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.exception.notfound.AlarmNotFoundException;
import com.prography1.eruna.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@RequiredArgsConstructor
public class AlarmItemProcessor implements ItemProcessor<DayOfWeek, Alarm> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmItemProcessor.class);
    private final AlarmRepository alarmRepository;

    @Override
    public Alarm process(DayOfWeek item) throws Exception {
//        LocalDate localDate = LocalDate.now();
//        String day = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);
//
//        String storedDay = item.getDayOfWeekId().getDay().toString();
//        if(day.equals(storedDay)) {
//
//            LOGGER.info("Day: " + day.toString());
//            return alarmRepository.findById(item.getAlarm().getId()).orElseThrow(() -> new AlarmNotFoundException(BaseResponseStatus.DATABASE_ERROR));
//        }
//        return null;
    }
}
