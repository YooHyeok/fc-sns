package com.fc.sns.model;

import com.fc.sns.model.entity.AlarmEntity;
import com.fc.sns.model.entity.PostEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class Alarm {
    private Integer id;
    private User user;
    private AlarmType alarmType;
    private AlarmArgs alarmArgs;
    private Timestamp registeredAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public static Alarm fromEntity(AlarmEntity alarmEntity) {
        return new Alarm(
                alarmEntity.getId(),
                User.fromEntity(alarmEntity.getUser()), //UserEntity를 User로 변환
                alarmEntity.getAlarmType(),
                alarmEntity.getAlarmArgs(),
                alarmEntity.getRegisteredAt(),
                alarmEntity.getUpdatedAt(),
                alarmEntity.getDeletedAt()
        );
    }

}
