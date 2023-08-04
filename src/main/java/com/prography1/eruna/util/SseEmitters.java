package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.repository.GroupRepository;
import com.prography1.eruna.domain.repository.GroupUserRepository;
import com.prography1.eruna.domain.repository.WakeUpCacheRepository;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.service.WakeupService;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
@Slf4j
@RequiredArgsConstructor
public class SseEmitters {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final WakeUpCacheRepository wakeUpCacheRepository;
    private final WakeupService wakeupService;
    private final GroupUserRepository groupUserRepository;
    private final GroupRepository groupRepository;

    public SseEmitter add(Long groupId, String uuid) {
        SseEmitter emitter = new SseEmitter(10 * 1000L);
        String key = generateKey(groupId, uuid);

////        this.emitters.add(emitter);
//        if (emitters.containsKey(key)) {
//            return emitters.get(key);
//        }
//        this.emitters.put(key, emitter);
        log.info("new emitter added: {}", emitter);
        log.info("emitter list size: {}", emitters.size());

        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            this.emitters.remove(key);    // 만료되면 맵에서 삭제
        });
        emitter.onTimeout(() -> {
            log.info("onTimeout callback");
            emitter.complete();
        });
        emitter.onError((c) -> {
            emitter.completeWithError(c.getCause());
        });

        emitters.putIfAbsent(key, emitter);

        return emitter;
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
        list.sort(Comparator.comparing(UserResDto.WakeupDto::getWakeupTime));
        return list;
    }

    public void sendWakeupInfo(Long groupId, String uuid) {
        List<UserResDto.WakeupDto> list = wakeUpCacheRepository.getWakeupDtoList(groupId);
        String key = generateKey(groupId, uuid);

//        if(sseEmitter == null){
//            sseEmitter = new SseEmitter(30 * 60L * 1000);
//            emitters.put(key, sseEmitter);
//        }
        /**
         * 캐싱된 데이터가 없으면 DB에서 캐싱과 동시에 그룹 유저들을 찾아 리스트를 반환한다.
         */

        if (list.isEmpty()) {
            Groups group = groupRepository.findById(groupId).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_GROUP));
            List<GroupUser> groupUsers = groupUserRepository.findByGroupsForScheduler(group);
            list = wakeUpCacheRepository.createGroupUsersCache(list, groupId, groupUsers);
        }

        Object[] wakeupDtos = list.toArray();
        SseEmitter sseEmitter = emitters.get(key);

//        try {
//            sseEmitter.send(SseEmitter.event()
//                    .name("wakeupInfo")
//                    .data(list.toArray()));
//            log.info("SSE SEND!! : " + groupId);
//
//        } catch (IOException e) {
//            log.error("SSE ERROR : " + e.getMessage());
//            sseEmitter.completeWithError(e.getCause());
//        }
        send(sseEmitter, list, groupId, uuid);

    }

    public void sendWakeupInfo2All(Long groupId) {
        List<UserResDto.WakeupDto> wakeupDtoList = wakeUpCacheRepository.getWakeupDtoList(groupId);

        for (UserResDto.WakeupDto wakeupDto : wakeupDtoList) {
            String uuid = wakeupDto.getUuid();

            String key = generateKey(groupId, uuid);
            SseEmitter sseEmitter = emitters.get(key);
            /**
             * 아직 기상 하지 않은 유저는 SSE 연결이 되어 있지 않음
             */
            if (sseEmitter == null) continue;
//            try {
//                sseEmitter.send(SseEmitter.event()
//                        .name("wakeupInfo")
//                        .data(wakeupDtoList.toArray()));
//                log.info("SSE SEND!! : " + groupId);
//
//            } catch (IOException e) {
//                log.error("SSE ERROR : " + e.getMessage());
//                sseEmitter.completeWithError(e.getCause());
//            }
            Object[] wakeupDtos = wakeupDtoList.toArray();
            send(sseEmitter, wakeupDtoList, groupId, uuid);
        }
    }

    private void send(SseEmitter sseEmitter, List<UserResDto.WakeupDto> wakeupDtos, Long groupId, String uuid) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("wakeupInfo")
                        .data(wakeupDtos));
                log.info("SSE SEND!! : " + wakeupDtos);

//                sseEmitter.complete();
            } catch (IOException | IllegalStateException e) {
                log.error("SSE ERROR : " + e.getMessage());
                sseEmitter.completeWithError(e.getCause());
                emitters.remove(generateKey(groupId, uuid));
                executor.shutdown();
            }
        });
        executor.shutdown();
    }

    private String generateKey(Long groupId, String uuid) {
        return "SSE." + groupId + "." + uuid;
    }
}
