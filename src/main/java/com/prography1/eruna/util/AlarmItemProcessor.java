package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@RequiredArgsConstructor
public class AlarmItemProcessor implements ItemProcessor<DayOfWeek, Alarm> {

    private final AlarmRepository alarmRepository;

    @Override
    public Alarm process(DayOfWeek item) throws Exception {
        LocalDate localDate = LocalDate.now();
        String day = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);

        String storedDay = item.getDayOfWeekId().getDay().toString();
        if(day.equals(storedDay)) {

            System.out.println("Day: " + day.toString());
            return alarmRepository.findById(item.getAlarm().getId()).orElseThrow(() -> new BaseException(BaseResponseStatus.DATABASE_ERROR));
        }
        return null;
    }
}
