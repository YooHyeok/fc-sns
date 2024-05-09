package com.fc.sns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fc.sns.controller.request.UserJoinRequest;
import com.fc.sns.controller.request.UserLoginRequest;
import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.model.User;
import com.fc.sns.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc // API 형태의 Controller Test
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    public void 회원가입() throws Exception {
        String userName = "userName";
        String password = "password";

        Mockito.when(userService.join(userName, password)).thenReturn(Mockito.mock(User.class));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/users/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        //TODO : add request body
                        .content(objectMapper.writeValueAsBytes(new UserJoinRequest(userName, password)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    public void 회원가입시_이미_회원가입된_userName으로_회원가입을_하는경우_에러반환() throws Exception {
        String userName = "userName";
        String password = "password";

        Mockito.when(userService.join(userName, password)).thenThrow(new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/users/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                //TODO : add request body
                                .content(objectMapper.writeValueAsBytes(new UserJoinRequest(userName, password)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isConflict()); // 충돌...
    }

    @Test
    public void 로그인() throws Exception { // 로그인시_회원가입이_안된_userName을_입력할경우_에러반환
        String userName = "userName";
        String password = "password";

        Mockito.when(userService.login(userName, password)).thenReturn("test_token");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                //TODO : add request body
                                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    public void 로그인시_회원가입이_안된_userName을_입력할경우_에러반환() throws Exception {
        String userName = "userName";
        String password = "password";

        Mockito.when(userService.login(userName, password)).thenThrow(new SnsApplicationException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                //TODO : add request body
                                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void 로그인시_틀린_password를_입력할경우_에러반환() throws Exception {
        String userName = "userName";
        String password = "password";

        Mockito.when(userService.login(userName, password)).thenThrow(new SnsApplicationException(ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                //TODO : add request body
                                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // invalid한 패스워드를 반환하였으므로, 인증되지않은 isUnauthorized
    }
}
