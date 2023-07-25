package com.prography1.eruna.domain.repository;

import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.entity.Wakeup;
import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import com.prography1.eruna.util.RedisGenKey;
import com.prography1.eruna.web.UserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class WakeUpCacheRepository {
    private final RedisTemplate<String, UserResDto.WakeupDto> redisTemplate;

    public void addSleepUser(Long groupId, UserResDto.WakeupDto wakeupDto){
        String cachingKey = RedisGenKey.generateGroupKey(groupId);

//        if (Boolean.TRUE.equals(redisTemplate.hasKey(cachingKey)))
//            redisTemplate.delete(cachingKey);
        if(updateIfPresent(cachingKey, wakeupDto)) return;

        redisTemplate.opsForList().rightPush(cachingKey, wakeupDto);
        redisTemplate.expire(cachingKey, 60, TimeUnit.MINUTES);
    }

    private int isExistDtoInList(List<UserResDto.WakeupDto> list, UserResDto.WakeupDto wakeupDto, Long size){
        if(size == 0) return -1;

        for(int i = 0; i < size; i++){
            UserResDto.WakeupDto dto = list.get(i);

            if (dto.getUuid().equals(wakeupDto.getUuid())) return i;
        }
        return -1;
    }

    private Long getListSize(String key){
        Long size = redisTemplate.opsForList().size(key);
        if(Objects.isNull(size)) return 0L; //list 존재안하면 size가 0

        return size;
    }

    private boolean updateIfPresent(String cachingKey, UserResDto.WakeupDto wakeupDto){
        Long size = getListSize(cachingKey);
        List<UserResDto.WakeupDto> list = redisTemplate.opsForList().range(cachingKey, 0, size);

        if (list == null) list =  new ArrayList<>();

        redisTemplate.expire(cachingKey, 60, TimeUnit.MINUTES);

        int index = isExistDtoInList(list, wakeupDto, size);
        if(index > -1){
            redisTemplate.opsForList().set(cachingKey, index, wakeupDto);
//            list.set(index, wakeupDto);
            return true;
        }
        return false;
    }

    public List<UserResDto.WakeupDto> findWakeupInfo(Long groupId){
        String cachingKey = RedisGenKey.generateGroupKey(groupId);
        Long size = getListSize(cachingKey);
        List<UserResDto.WakeupDto> list = redisTemplate.opsForList().range(cachingKey, 0, size);

        if (list == null) list =  new ArrayList<>();

        return list;
    }

    public void updateWakeupInfo(Long groupId, String uuid, String nickname, String phoneNum) {
        String key = RedisGenKey.generateGroupKey(groupId);
        UserResDto.WakeupDto wakeupDto = UserResDto.WakeupDto.builder()
                .uuid(uuid)
                .nickname(nickname)
                .phoneNum(phoneNum)
                .wakeup(true)
                .wakeupDate(LocalDate.now().toString())
                .wakeupTime(LocalTime.now().toString())
                .build();

        updateIfPresent(key, wakeupDto);
    }

    public boolean isAllWakeup(List<UserResDto.WakeupDto> list){
        for(UserResDto.WakeupDto wakeupDto : list){
            if (!wakeupDto.getWakeup()) {
                return false;
            }
        }

        return true;
    }

    public boolean isCachedGroupId(Long groupId){
        String key = RedisGenKey.generateGroupKey(groupId);
        Long size = redisTemplate.opsForList().size(key); 
        return size != null && size != 0;
    }

    public boolean deleteCachedGroup(Long groupId){
        String key = RedisGenKey.generateGroupKey(groupId);
        if(isCachedGroupId(groupId)){
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    public List<UserResDto.WakeupDto> createGroupUsersCache(List<UserResDto.WakeupDto> list, Long groupId, List<GroupUser> groupUsers ){

        for(GroupUser groupUser : groupUsers){
            UserResDto.WakeupDto wakeupDto = UserResDto.WakeupDto.fromUser(groupUser.getUser(), groupUser.getNickname(), groupUser.getPhoneNum());
            addSleepUser(groupId, wakeupDto);
            list.add(wakeupDto);
        }
        return list;
    }
}
