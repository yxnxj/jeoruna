package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.DayOfWeek;
import com.prography1.eruna.domain.enums.Week;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class DayOfWeekRowMapper implements RowMapper<DayOfWeek> {
    private final AlarmRepository alarmRepository;


    @Override
    public DayOfWeek mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long alarmId = rs.getLong("alarm_id");
        Alarm alarm = alarmRepository.findById(alarmId).orElseThrow(() -> new BaseException(BaseResponseStatus.DATABASE_ERROR));
        DayOfWeek.DayOfWeekId dayOfWeekId = DayOfWeek.DayOfWeekId.builder()
                .alarmId(alarmId)
                .day(Week.valueOf(rs.getString("day")))
                .build();

        DayOfWeek dayOfWeek = DayOfWeek.builder()
                .dayOfWeekId(dayOfWeekId)
                .alarm(alarm)
                .build();

        return dayOfWeek;
    }
}
