package com.fc.sns.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 회원가입 Join 파라미터용 요청 객체
 * requestBody로 받을 요청 커맨드객체로 사용된다.
 */

@AllArgsConstructor
@Getter
@ToString
public class UserLoginRequest {
    private String name;
    private String password;
}
