package com.fc.sns.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원가입 Join 파라미터용 요청 객체
 * requestBody로 받을 요청 커맨드객체로 사용된다.
 */

@AllArgsConstructor
@Getter
public class UserJoinRequest {
    private String userName;
    private String password;
}
