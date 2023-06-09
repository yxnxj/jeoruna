package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.entity.Wakeup;
import com.prography1.eruna.domain.repository.AlarmRepository;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.domain.repository.UserRepository;
import com.prography1.eruna.domain.repository.WakeupRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WakeupService {
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final GroupRepository groupRepository;
    private final WakeupRepository wakeupRepository;

    private Wakeup save(UserResDto.WakeupDto wakeupDto, Long groupId){
        User user = userRepository.findByUuid(wakeupDto.getUuid()).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_UUID_TOKEN));
        Groups group = groupRepository.findById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_GROUP));
        Alarm alarm = alarmRepository.findByGroups(group).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ALARM));

        Wakeup wakeup = Wakeup.builder()
                .alarm(alarm)
                .wakeupCheck(true)
                .date(LocalDate.now())
                .wakeupTime(LocalDateTime.now())
                .user(user)
                .build();

        return wakeupRepository.save(wakeup);
    }

    public List<Wakeup> saveAll(List<UserResDto.WakeupDto> list, Long groupId) {
        List<Wakeup> wakeupList = new ArrayList<>();

        for(UserResDto.WakeupDto wakeupDto : list){
            wakeupList.add(save(wakeupDto, groupId));
        }

        return wakeupList;
    }
}
