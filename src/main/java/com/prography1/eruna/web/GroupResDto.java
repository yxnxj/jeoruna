package com.prography1.eruna.web;

import com.prography1.eruna.domain.entity.Alarm;
import com.prography1.eruna.domain.entity.GroupUser;
import com.prography1.eruna.domain.entity.Groups;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GroupResDto {

    private GroupResDto(){}

    @Schema(title = "Group 생성 시 GroupId 반환")
    @Getter
    @AllArgsConstructor
    public static class CreatedGroup {
        @Schema(description = "GroupId", example = "1")
        private Long groupId;
    }

    @Schema(title = "재발급된 Group Code")
    @Getter
    @AllArgsConstructor
    public static class NewGroupCode {
        @Schema(description = "그룹코드", example = "s1a0d1")
        private String groupCode;
    }

    @Schema(title = "닉네임 중복 체크")
    @Getter
    @AllArgsConstructor
    public static class IsValidNickname {
        @Schema(description = "nickname 중복 여부 boolean 값, true를 반환하면 사용 가능한 닉네임이다", example = "true")
        private Boolean isValid;

    }

    @Schema(title = "그룹 정보")
    @Getter
    @AllArgsConstructor
    public static class GroupInfo {
        @Schema(description = "그룹 코드", example = "s1a0d1")
        private String code;

        @Schema(description = "멤버 정보 리스트")
        private List<Member> members;

        @Schema(description = "그룹 알람 정보")
        private AlarmInfo alarm;

        @Schema(title = "멤버 정보")
        @AllArgsConstructor
        @Getter
        private static class Member {
            @Schema(description = "멤버 닉네임", example = "피치푸치")
            String nickname;
            @Schema(description = "해당 멤버가 그룹장인지", example = "true")
            Boolean isHost;
            @Schema(description = "전화번호", example = "01012345678")
            String phoneNum;
            @Schema(description = "멤버 uuid", example = "123-5678-asdf-asfg")
            String uuid;

            private static Member fromGroupUser(Groups group, GroupUser groupUser){
                boolean isHost = false;
                if(group.getHost() == groupUser.getUser()){
                    isHost = true;
                }
                return new Member(groupUser.getNickname(), isHost, groupUser.getPhoneNum(),
                        groupUser.getUser().getUuid());
            }

        }

        @Schema(title = "그룹 알람 정보")
        @AllArgsConstructor
        @Getter
        private static class AlarmInfo{
            @Schema(description = "알람 소리", example = "sound_track_1")
            String sound;
            @Schema(description = "알람 시", example = "13")
            Integer hours;
            @Schema(description = "알람 분", example = "30")
            Integer minutes;
            @Schema(description = "알람 반복 요일", example = "[\"MON\", \"SUN\", \"WED\"]")
            List<String> weekList;

            private static AlarmInfo fromAlarm(Alarm alarm){
                List<String> weekList = new ArrayList<>();
                alarm.getWeekList().forEach(week-> weekList.add(week.getDayOfWeekId().getDay().name()));
                return new AlarmInfo(alarm.getAlarmSound(), alarm.getAlarmTime().getHour(),
                        alarm.getAlarmTime().getMinute(), weekList);
            }
        }

        public static GroupInfo fromGroup(Groups group) {
            List<Member> memberList = new ArrayList<>();
            group.getGroupUserList().forEach(groupUser -> memberList.add(Member.fromGroupUser(group, groupUser)));
            return new GroupInfo(group.getCode(), memberList, AlarmInfo.fromAlarm(group.getAlarm()));
        }
    }
}
