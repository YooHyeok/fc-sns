package com.fc.sns.model.event;

import com.fc.sns.model.AlarmArgs;
import com.fc.sns.model.AlarmType;
import com.fc.sns.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmEvent {
    private Integer receiverUserId;
    private AlarmType alarmType;
    private AlarmArgs alarmArgs;
}
