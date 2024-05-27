package com.fc.sns.service;

import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.model.Alarm;
import com.fc.sns.model.User;
import com.fc.sns.model.entity.AlarmEntity;
import com.fc.sns.model.entity.LikeEntity;
import com.fc.sns.model.entity.UserEntity;
import com.fc.sns.repository.AlarmEntityRepository;
import com.fc.sns.repository.UserEntityRepository;
import com.fc.sns.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEntityRepository userEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    public User loadByUsername(String userName) {
        return userEntityRepository.findByUserName(userName)
                .map(User::fromEntity)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));
    }

    @Transactional
    public User join(String userName, String password) {
        // 회원 가입 하려는 userName으로 회원 가입된 user가 있는지
        userEntityRepository.findByUserName(userName).ifPresent( // ifPresent(): 값이 존재한다면 블록실행
                it -> {// 회원이 존재한다면 Exception throw
                    throw new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME, String.format("%s is duplicated", userName));
                }
        );

        // 회원 가입 진행 = user를 등록
        UserEntity savedUser = userEntityRepository.save(UserEntity.of(userName, encoder.encode(password)));
        return User.fromEntity(savedUser);
    }

    // TODO: implement
    public String login(String userName, String password) {
        // 회원가입 여부 체크
        UserEntity userEntity = userEntityRepository.findByUserName(userName)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));

        // 비밀번호 체크
//        if(!userEntity.getPassword().equals(password)) {
        if(!encoder.matches(password, userEntity.getPassword())) { // 평문, 암호문 비교 - 일치하지 않으면 exception
            throw new SnsApplicationException(ErrorCode.INVALID_PASSWORD);
        }
        
        // 토큰 생성
        String token = JwtTokenUtils.generateToken(userName, secretKey, expiredTimeMs);
        return token;
    }

    @Transactional(readOnly = true)
    public Page<Alarm> alarmList(String userName, Pageable pageable) {
        UserEntity userEntity = userEntityRepository.findByUserName(userName)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));
        return alarmEntityRepository.findAllByUser(userEntity, pageable)
                .map(Alarm::fromEntity);
    }

    /**
     * 이전에 사용하던 방식은 UserEntity를 직접 조회하여 데이터가 orElseThrow를 발생시켰다.
     * 이 대신 getId(One,referencedById)를 통해 프록시객체를 가져오고, try~catch구문으로 감싼뒤
     * 데이터를 실제 사용할때 Exception을 catch하여 Error를 출력하도록 리팩토링 할 수도 있지만,
     * 현재 컨트롤러로 부터 Authentication객체의 principal를 통해 User객체를 넘겨받도록 하였다.
     * 이 작업을 통해 굳이 User를 조회하지 않아도 되고, 또한 AlarmEntity의 연관 엔티티인 UserEntity에 대한
     * Lazy 패치전략 적용이 가능해진다. (@EntityGraph를 사용하는 불필요한 Fetch를 하지 않아도 됨)
     * (pricipal로 가져온 User객체를 넘겨주면 되므로 다른 엔티티에서는 따로 엔티티 조회가 필요하긴 함.)
     * @param user
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public Page<Alarm> alarmListRefact(User user, Pageable pageable) {
        return alarmEntityRepository.findAllByUserId(user.getId(), pageable)
                .map((alarmEntity) -> Alarm.fromEntityRefact(alarmEntity, user));
    }
}
