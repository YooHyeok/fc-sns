package com.fc.sns.service;

import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.model.AlarmArgs;
import com.fc.sns.model.AlarmType;
import com.fc.sns.model.entity.AlarmEntity;
import com.fc.sns.model.entity.PostEntity;
import com.fc.sns.model.entity.UserEntity;
import com.fc.sns.repository.AlarmEntityRepository;
import com.fc.sns.repository.EmitterRepository;
import com.fc.sns.repository.PostEntityRepository;
import com.fc.sns.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final static Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final static String ALARM_NAME = "alarm";
    private final EmitterRepository emitterRepository;

    private final AlarmEntityRepository alarmEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final PostEntityRepository postEntityRepository;


    /**
     * Map으로부터 알람 수신 회원 Id로 Emitter를 반환받는다. <br/>
     * (이때 해당 수신자가 접속을 종료했을 경우 onCompletion 콜백에 의해 지워지므로 Optional로 null값을 반환받는다.) <br/>
     * null이 아니라면 수신자의 서버가 연결되어 있는 상태이므로 <br/>
     * 반환받은 알람 수신자의 Emitter에 알람 관련 새로운 메시지를 전송하게 된다.
     * @param alarmId EventSource가 Sse의 알람을 식별할 ID (연결 유지 등?)
     * @param userId 알람을 받을 글 작성자
     */
    public void send(Integer alarmId, Integer userId) {

        emitterRepository.get(userId).ifPresentOrElse(sseEmitter -> {
            try {
                sseEmitter.send(
                        SseEmitter.event()
                        .id(alarmId.toString())
                        .name(ALARM_NAME)
                        .data("new alarm")
                );
            } catch (IOException e) {
                emitterRepository.delete(userId);
                throw new SnsApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
            }
        }, () -> log.info("No emitter founded"));

    }

    public void send(AlarmType alarmType, AlarmArgs alarmArgs, Integer receiverUserId) {
/*        UserEntity userEntity = userEntityRepository.findById(receiverUserId)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND));*/
        // UserEntity 대신 PostEntity 재조회 : save() 이후 찰라의 순간 글이 삭제될 수도 있기 때문 더 효율적인 조회
        PostEntity postEntity = postEntityRepository.findById(receiverUserId)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not founded", receiverUserId)));
        AlarmEntity alarmEntity = alarmEntityRepository.save(
                AlarmEntity.of(
//                        userEntity, // post 작성자 (알람 수신 대상자)
                        postEntity.getUser(), // post 작성자 (알람 수신 대상자)
                        alarmType, //알람 타입
                        alarmArgs // 알람 고유정보 - comment 작성자, 게시글 번호
                )
        );
//        emitterRepository.get(userEntity.getId()).ifPresentOrElse(sseEmitter -> {
        emitterRepository.get(postEntity.getUser().getId()).ifPresentOrElse(sseEmitter -> {
            try {
                sseEmitter.send(
                        SseEmitter.event()
                                .id(alarmEntity.getId().toString())
                                .name(ALARM_NAME)
                                .data("new alarm")
                );
            } catch (IOException e) {
//                emitterRepository.delete(userEntity.getId());
                emitterRepository.delete(postEntity.getUser().getId());
                throw new SnsApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
            }
        }, () -> log.info("No emitter founded"));

    }

    /**
     * 클라이언트의 EventSource와 서버의 SseEmitter의 Stream을 연결한다 <br/>
     * Alarm 페이지에 최초 진입시 API에 의해 호출된다. <br/>
     * Sse의 Stream 시작되며, 알람 수신 회원 ID와 고유 SseEmitter를 Map에 등록한다. <br/>
     * SseEmitter의 send()메소드에 의해 클라이언트에 전송하게 된다. <br/>
     * (send() 호출이 스케줄러에 등록되고 API에서 SseEmitter를 반환하고 난 뒤 호출됨) <br/>
     * @param userId
     * @return
     */
    public SseEmitter connectNotification(Integer userId) {
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(userId, sseEmitter);
        sseEmitter.onCompletion(() -> emitterRepository.delete(userId)); // Disconnect시 동작
        sseEmitter.onTimeout(() -> emitterRepository.delete(userId)); // 연결 시간 초과시 동작

        try {
            sseEmitter.send(
                    SseEmitter.event()
                        .id("id")
                        .name(ALARM_NAME) // EventSource의 addEventListner로 등록한 이벤트명과 일치하는 이벤트명을 입력해준다.
                        .data("connect completed")
            );
        } catch (IOException e) {
            throw new SnsApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
        }
        return sseEmitter;
    }
}
