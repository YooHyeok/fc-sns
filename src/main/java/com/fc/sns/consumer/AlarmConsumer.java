package com.fc.sns.consumer;

import com.fc.sns.model.entity.PostEntity;
import com.fc.sns.model.entity.UserEntity;
import com.fc.sns.model.event.AlarmEvent;
import com.fc.sns.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmConsumer {

    private final AlarmService alarmService;

    @KafkaListener(topics = "${spring.kafka.topic.alarm}")
    public void consumeAlarm(AlarmEvent event, Acknowledgment ack) {
        log.info("Consume the event {}", event);
        alarmService.send(event.getAlarmType(), event.getAlarmArgs(), event.getReceiverUserId());
        ack.acknowledge();
    }
}
