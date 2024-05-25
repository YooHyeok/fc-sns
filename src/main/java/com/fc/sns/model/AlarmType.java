package com.fc.sns.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor // final field나 @NonNull에 해당하는 필드만을 매개변수로 받는 생성자를 생성한다. (꼭 필요한 필드만을 초기화 할 수 있도록 생성자를 만들어준다)
@Getter
public enum AlarmType {
    NEW_COMMENT_ON_POST("new comment!"),
    NEW_LIKE_ON_POST("new like!"),
    ;

    private final String alarmText;
}
