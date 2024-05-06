package com.fc.sns.controller;

import com.fc.sns.controller.request.UserJoinRequest;
import com.fc.sns.controller.request.UserLoginRequest;
import com.fc.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // TODO: implement
    @PostMapping("/join")
    public void join(@RequestBody UserJoinRequest request) {
        userService.join(request.getUserName(), request.getPassword());
    }

    // TODO: implement
    @PostMapping("/login")
    public void login(@RequestBody UserLoginRequest request) {
        userService.login(request.getUserName(), request.getPassword());
    }
}
