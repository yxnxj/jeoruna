package com.prography1.eruna.service;

import com.google.firebase.messaging.*;
import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.enums.AlarmSound;
import com.prography1.eruna.domain.repository.GroupUserRepository;
import com.prography1.eruna.domain.repository.UserRepository;
import com.prography1.eruna.response.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.prography1.eruna.response.BaseResponseStatus.USER_NOT_FOUND;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final GroupUserRepository groupUserRepository;
    private final WakeupService wakeupService;

    private final FirebaseMessaging firebaseMessaging;

    public String joinByUUID(String fcmToken) {
        String uuidToken = UUID.randomUUID().toString();
        User user = User.join(uuidToken, fcmToken);
        userRepository.save(user);
        return uuidToken;
    }

    /**
     * ChoYeonJun add
     *
     * 알람을 깨우는 푸시 메시지를 firebase cloud messaging API를 통해 보낸다.
     * TODO : 푸시 메시지 문구 협의 필요
     */
    public String pushMessage(String fcmToken){
        return pushMessage(fcmToken, AlarmSound.ALARM_SIU.getFilename());
    }
    public String pushMessage(String fcmToken, String filename) {

        /**
         * Client에서 onNotification 이벤트로 알람을 받기 때문에 Message에 notification을 꼭 넣어주어야 알람이 발생한다.
         */
        Notification notification = Notification.builder()
                .setTitle("push alarm")
                .setBody("push wake up")
                .build();

        Message msg = Message.builder()
                .setNotification(notification)
                .setToken(fcmToken)
                .setApnsConfig(
                        ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setSound(filename)
                                        .build())
                                .build()
                )
//                .putData("sound", "siu, default")
                .build();
        try {
            String response = firebaseMessaging.send(msg);
            log.info("response : " + response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error(e.getMessage()  + ", token: " + fcmToken);
            if (e.getMessagingErrorCode() == MessagingErrorCode.INTERNAL){
                wakeupService.updateWakeupInfo(fcmToken);
            }
            return e.getMessage();
//            throw new BaseException(BaseResponseStatus.INVALID_FCM_TOKEN);
        }
    }

    /**
     * ChoYeonJun add
     * 임의의 메시지를 보내 token이 유효한지 체크한다
     *
     * TODO : 빈 데이터를 가진 임의의 메시지 인스턴스가 클라이언트단에 어떻게 전달이 되는지 확인 필요
     */
    public Boolean isValidFCMToken(String fcmToken) {

        Message message = Message.builder()
                .setToken(fcmToken)
                .build();
        try {
            firebaseMessaging.send(message);
            return true;
        } catch (FirebaseMessagingException fme) {
            log.error("Firebase token verification exception : " + fme.getMessage());
            log.error("FCM error code : " + fme.getMessagingErrorCode());
            return false;
        }
    }
    public boolean isUserExist(String uuid){
        return userRepository.existsByUuid(uuid);
    }

    public User findByUUID(String uuid){
        return userRepository.findByUuid(uuid).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
    }

    public Long findGroupIdByUUID(String uuid) {
        User user = userRepository.findByUuid(uuid).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        Optional<GroupUser> groupUser = groupUserRepository.findByUser(user);
        if(groupUser.isEmpty())
            return null;
        else
            return groupUser.get().getGroups().getId();
    }
}
