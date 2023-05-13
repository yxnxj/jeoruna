package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.User;
import com.prography1.eruna.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;

    public String joinByUUID() {
        String uuidToken = UUID.randomUUID().toString();
        User user = User.join(uuidToken);
        userRepository.save(user);
        return uuidToken;
    }
}
