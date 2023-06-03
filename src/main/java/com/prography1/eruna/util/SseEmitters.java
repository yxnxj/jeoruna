package com.prography1.eruna.util;

import com.prography1.eruna.domain.entity.Wakeup;
import com.prography1.eruna.domain.repository.WakeUpCacheRepository;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class SseEmitters {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final WakeUpCacheRepository wakeUpCacheRepository;
    public SseEmitter add(Long groupId ,SseEmitter emitter) {
//        this.emitters.add(emitter);
        this.emitters.put(groupId, emitter);
        log.info("new emitter added: {}", emitter);
        log.info("emitter list size: {}", emitters.size());
        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            this.emitters.remove(groupId);    // 만료되면 맵에서 삭제
        });
        emitter.onTimeout(() -> {
            log.info("onTimeout callback");
            emitter.complete();
        });

        return emitter;
    }

    public List<UserResDto.WakeupDto> sendWakeupInfo(Long groupId){
        SseEmitter emitter = emitters.get(groupId);
        return sendWakeupInfo(groupId, emitter);
    }
    
    public List<UserResDto.WakeupDto> sendWakeupInfo(Long groupId, SseEmitter sseEmitter){
        List<UserResDto.WakeupDto> lists = wakeUpCacheRepository.findWakeupInfo(groupId);
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("allWakeUp")
                .data(lists);

        for(UserResDto.WakeupDto wakeupDto : lists){
            if (!wakeupDto.getWakeup()) {
                event = SseEmitter.event()
                        .name("wakeupInfo")
                        .data(lists);
            }
        }
        
        try {
            sseEmitter.send(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return lists;
    }
}
