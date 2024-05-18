package com.fc.sns.controller.response;

import com.fc.sns.model.User;
import com.fc.sns.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 순수 조회가 아닌 연관관계에 의해 함께 반환되는 응답객체
 */
@Getter
@AllArgsConstructor
public class UserResponse {

    private Integer id;
    private String userName;
    private UserRole role;

    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getUserRole()
        );
    }

}
