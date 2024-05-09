package com.fc.sns.service;

import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.fixture.UserEntityFixture;
import com.fc.sns.model.entity.UserEntity;
import com.fc.sns.repository.UserEntityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserEntityRepository userEntityRepository;

    @MockBean
    private BCryptPasswordEncoder encoder;

    @Test
    void 회원가입이_정상적으로_동작하는_경우() {
        String userName = "userName";
        String password = "password";

        UserEntity fixture = UserEntityFixture.get(userName, password);


        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());
//        Mockito.when(userEntityRepository.save(ArgumentMatchers.any())).thenReturn(Optional.of(Mockito.mock(UserEntity.class)));
        Mockito.when(encoder.encode(password)).thenReturn("encrypt_password");
        Mockito.when(userEntityRepository.save(ArgumentMatchers.any())).thenReturn(fixture);
        Assertions.assertDoesNotThrow(() -> userService.join(userName, password));
    }

    @Test
    void 회원가입이_userName으로_회원가입한_유저가_이미_있는경우() {
        String userName = "userName";
        String password = "password";

        UserEntity fixture = UserEntityFixture.get(userName, password);

        //mocking
//        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(Mockito.mock(UserEntity.class)));
//        Mockito.when(userEntityRepository.save(ArgumentMatchers.any())).thenReturn(Optional.of(Mockito.mock(UserEntity.class)));
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        Mockito.when(encoder.encode(password)).thenReturn("encrypt_password");
        Mockito.when(userEntityRepository.save(ArgumentMatchers.any())).thenReturn(fixture);
        SnsApplicationException e = assertThrows(SnsApplicationException.class, () -> userService.join(userName, password));
        Assertions.assertEquals(ErrorCode.DUPLICATED_USER_NAME, e.getErrorCode());
    }

    @Test
    void 로그인이_정상적으로_동작하는_경우() {
        String userName = "userName";
        String password = "password";

        UserEntity fixture = UserEntityFixture.get(userName, password);

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        Mockito.when(encoder.matches(password, fixture.getPassword())).thenReturn(true);
        Assertions.assertDoesNotThrow(() -> userService.login(userName, password));
    }

    @Test
    void 로그인시_userName으로_회원가입한_유저가_없는_경우() {
        String userName = "userName";
        String password = "password";

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());
        SnsApplicationException e = assertThrows(SnsApplicationException.class, () -> userService.login(userName, password));
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void 로그인시_패스워드가_틀린_경우() {
        String userName = "userName";
        String password = "password";
        String wrongPassword = "wrongPassword";

        UserEntity fixture = UserEntityFixture.get(userName, password);

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        SnsApplicationException e = assertThrows(SnsApplicationException.class, () -> userService.login(userName, wrongPassword));
        Assertions.assertEquals(ErrorCode.INVALID_PASSWORD, e.getErrorCode());
    }
}