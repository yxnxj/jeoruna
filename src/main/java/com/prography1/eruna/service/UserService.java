package com.prography1.eruna.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.repository.UserRepository;
import com.prography1.eruna.web.UserReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final FirebaseMessaging firebaseMessaging;

    public String joinByUUID(String fcmToken) {
        String uuidToken = UUID.randomUUID().toString();
        User user = User.join(uuidToken, fcmToken);
        userRepository.save(user);
        return uuidToken;
    }



    /**
     * add by ChoYeonJun
     *
     * 알람을 깨우는 푸시 메시지를 firebase cloud messaging API를 통해 보낸다.
     * TODO : 푸시 메시지 문구 협의 필요
     *
     */

    public void pushMessage(String fcmToken) {

        Message msg = Message.builder()
                .setToken(fcmToken)
                .putData("body", "일어나세요!")
                .build();

        try {
            firebaseMessaging.send(msg);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * add by ChoYeonJun
     * 임의의 메시지를 보내 token이 유효한지 체크한다
     *
     * TODO : 빈 데이터를 가진 임의의 메시지 인스턴스가 클라이언트단에 어떻게 전달이 되는지 확인 필요
     * @param fcmToken
     * @return
     */
    public Boolean isValidFCMToken(String fcmToken) {
        Message message = Message.builder().setToken(fcmToken).build();
        try {
            FirebaseMessaging.getInstance().send(message);
            return true;
        } catch (FirebaseMessagingException fme) {
            log.error("Firebase token verification exception", fme);
            return false;
        }
    }
}
