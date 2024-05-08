package com.fc.sns.service;

import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.model.User;
import com.fc.sns.model.entity.UserEntity;
import com.fc.sns.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEntityRepository userEntityRepository;

    public User join(String userName, String password) {
        // 회원 가입 하려는 userName으로 회원 가입된 user가 있는지
        userEntityRepository.findByUserName(userName).ifPresent( // ifPresent(): 값이 존재한다면 블록실행
                it -> {// 회원이 존재한다면 Exception throw
                    throw new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME, String.format("%s is duplicated", userName));
                }
        );

        // 회원 가입 진행 = user를 등록
        UserEntity savedUser = userEntityRepository.save(UserEntity.of(userName, password));

        return User.fromEntity(savedUser);
    }

    // TODO: implement
    public String login(String userName, String password) {
        // 회원가입 여부 체크
        UserEntity userEntity = userEntityRepository.findByUserName(userName).orElseThrow(() -> new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME, ""));

        // 비밀번호 체크
        if(!userEntity.getPassword().equals(password)) {
            throw new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME, "");
        }
        
        // 토큰 생성

        return "";
    }
}
