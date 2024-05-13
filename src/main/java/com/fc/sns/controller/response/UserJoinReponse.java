package com.fc.sns.controller.response;

import com.fc.sns.model.User;
import com.fc.sns.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinReponse {

    private Integer id;
    private String userName;
    private UserRole role;

    public static UserJoinReponse fromUser(User user) {
        return new UserJoinReponse(
            user.getId(),
            user.getUsername(),
            user.getUserRole()
        );
    }

}
