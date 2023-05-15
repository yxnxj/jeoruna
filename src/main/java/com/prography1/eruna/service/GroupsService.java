package com.prography1.eruna.service;

import com.prography1.eruna.domain.entity.Groups;
import com.prography1.eruna.domain.repository.GroupUserRepository;
import com.prography1.eruna.domain.repository.GroupsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupsService {
    private final GroupsRepository groupsRepository;
    private final GroupUserRepository groupUserRepository;

    public Boolean isValidCode(String code){
        return groupsRepository.existsByCode(code);
    }

    public boolean isDuplicatedNickname(String code, String nickname) {
        Groups group = groupsRepository.findByCode(code);
        return groupUserRepository.existsByGroupsAndNickname(group, nickname);
    }
}
