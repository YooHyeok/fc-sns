package com.fc.sns.controller.response;

import com.fc.sns.model.User;
import com.fc.sns.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinResponse {

    private Integer id;
    private String userName;
    private UserRole role;

    public static UserJoinResponse fromUser(User user) {
        return new UserJoinResponse(
            user.getId(),
            user.getUsername(),
            user.getUserRole()
        );
    }

}
