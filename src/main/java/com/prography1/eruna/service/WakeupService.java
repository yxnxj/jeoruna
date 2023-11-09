package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.*;
import com.prography1.eruna.domain.repository.*;
import com.prography1.eruna.exception.UserNotFoundException;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.prography1.eruna.response.BaseResponseStatus.NOT_FOUND_GROUP;

@Service
@RequiredArgsConstructor
@Slf4j
public class WakeupService {
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final GroupRepository groupRepository;
    private final WakeupRepository wakeupRepository;
    private final GroupUserRepository groupUserRepository;
    private final WakeUpCacheRepository wakeUpCacheRepository;
    private final Scheduler scheduler;


    private Wakeup save(UserResDto.WakeupDto wakeupDto, Long groupId){
        User user = userRepository.findByUuid(wakeupDto.getUuid()).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("`%s` uuid를 갖는 user를 찾지 못했습니다.", wakeupDto.getUuid())));
        Groups group = groupRepository.findById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_GROUP));
        Alarm alarm = alarmRepository.findByGroups(group).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_ALARM));

        Wakeup wakeup = Wakeup.builder()
                .alarm(alarm)
                .wakeupCheck(true)
                .wakeupDate(LocalDate.now())
                .wakeupTime(LocalTime.parse(wakeupDto.getWakeupTime()))
                .user(user)
                .build();
        return wakeupRepository.save(wakeup);
    }

    public List<Wakeup> saveAll(List<UserResDto.WakeupDto> list, Long groupId) {
        List<Wakeup> wakeupList = new ArrayList<>();
        log.info("Group " + groupId +  " ALL WAKEUP!!");
        for(UserResDto.WakeupDto wakeupDto : list){
            wakeupList.add(save(wakeupDto, groupId));
            try {
                deleteFcmJob(wakeupDto.getUuid());
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        }

        return wakeupList;
    }

    private void deleteFcmJob(String uuid) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(uuid);
        scheduler.deleteJob(jobKey);
        log.info("fcm job delete : " + uuid);
    }

    public void updateWakeupInfo(Long groupId, String uuid){

        User user = userRepository.findByUuid(uuid).orElseThrow(() -> new UserNotFoundException(BaseResponseStatus.USER_NOT_FOUND, String.format("%s uuid를 갖는 user를 찾지 못했습니다.", uuid)));
        GroupUser groupUser = groupUserRepository.findGroupUserByUser(user).orElseThrow(() -> new BaseException(NOT_FOUND_GROUP));
        wakeUpCacheRepository.updateWakeupInfo(groupId, uuid, groupUser.getNickname(), groupUser.getPhoneNum());

        if(wakeUpCacheRepository.isAllWakeup(groupId)) {
            List<UserResDto.WakeupDto> list = wakeUpCacheRepository.getWakeupDtoList(groupId);
            saveAll(list, groupId);
//            wakeUpCacheRepository.deleteCachedGroup(groupId);
        }
    }

    public List<UserResDto.WakeupDto> findWakeupInfo(Long groupId) {
        List<UserResDto.WakeupDto> list = wakeUpCacheRepository.getWakeupDtoList(groupId);
        /**
         * 캐싱된 데이터가 없으면 DB에서 캐싱과 동시에 그룹 유저들을 찾아 리스트를 반환한다.
         */

        if (list.isEmpty()) {
            Groups group = groupRepository.findById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_GROUP));
            List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);
            list = wakeUpCacheRepository.createGroupUsersCache(list, groupId, groupUsers);
        }

        return list;
    }
}
