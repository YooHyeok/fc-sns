package com.fc.sns.model.entity;

import com.fc.sns.model.AlarmArgs;
import com.fc.sns.model.AlarmType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "\"alarm\"", indexes = {
        @Index(name = "user_id_idx", columnList = "user_id")
})
@SQLDelete(sql = "UPDATE \"alarm\" SET deleted_at = NOW() where id = ?") // 삭제시 삭제전 발생
@Where(clause = "deleted_at is NULL") // SELECT시 deleted_at이 null인 경우만 조회되도록 where 절 추가
@TypeDef(name="jsonb", typeClass = JsonBinaryType.class)
public class AlarmEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;// 알람 수신 대상자

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    @Type(type = "jsonb") // postgres에는 json과 jsonb가 있음 - json은 일반저장, jsonb는 압축을하여 저장 및 index를 걸 수 있음.
    @Column(columnDefinition = "json")
    private AlarmArgs alarmArgs;

    @Column(name = "register_at")
    private Timestamp registeredAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @PrePersist
    void registeredAt() {
        this.registeredAt = Timestamp.from(Instant.now());
    }
    @PreUpdate
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public static AlarmEntity of(UserEntity userEntity, AlarmType alarmType, AlarmArgs alarmArgs) {
        AlarmEntity alarmEntity = new AlarmEntity();
        alarmEntity.setUser(userEntity);
        alarmEntity.setAlarmType(alarmType);
        alarmEntity.setAlarmArgs(alarmArgs);
        return alarmEntity;
    }

}
