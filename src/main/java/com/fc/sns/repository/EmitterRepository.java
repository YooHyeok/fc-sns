package com.fc.sns.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 수신자 별 SseEmitter 등록 클래스
 * API 호출 즉, 알람 페이지 진입 시점에 AlarmService의 connectionNotification()에 의해
 * 페이지에 진입한 수신자와 수신자의 SseEmitter가 save된다.
 * 좋아요, 댓글 기능 호출시 송신자가 글작성자(수신자)를 기준으로 SseEmitter를 꺼내 이벤트를 발생시킨다
 */
@Slf4j
@Repository
public class EmitterRepository {
    private Map<String, SseEmitter> emitterMap = new HashMap<>();

    public SseEmitter save(Integer userId, SseEmitter sseEmitter) {
        final String key = getKey(userId);
        emitterMap.put(key, sseEmitter);
        log.info("Set sseEmitter {}", userId);
        return sseEmitter;
    }

    public void delete(Integer userId) {
        emitterMap.remove(getKey(userId));
    }

    public Optional<SseEmitter> get(Integer userId) {
        final String key = getKey(userId);
        log.info("Get sseEmitter {}", userId);
        return Optional.ofNullable(emitterMap.get(key));
    }

    private String getKey(Integer userId) {
        return "Emitter:UID:" + userId;
    }
}
