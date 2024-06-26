package com.fc.sns.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmArgs {
    private Integer fromUserId; // 알람 발생시킨 회원
    private Integer targetId; // 알람 발생 주체 (Post, Comment 등)
}
