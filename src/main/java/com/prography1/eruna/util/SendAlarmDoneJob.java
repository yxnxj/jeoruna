package com.prography1.eruna.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Slf4j
public class SendAlarmDoneJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        /**
         * 알람이 끝났음을 보낸다.
         */
        log.info("Send SSE FCM is Done");
    }

    public static Trigger setAfterAlarmTrigger(int seconds, LocalTime localTime){
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime.plusSeconds(seconds));
        Date date = java.sql.Timestamp.valueOf(localDateTime);
        return TriggerBuilder.newTrigger()
                .startAt(date)
                .build();
    }
}
