package com.fc.sns.model.entity;

import com.fc.sns.model.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "\"user\"") // postgreSQL은 User 테이블이 기본적으로 이미 존재하기 떄문에 ""를 붙혀야 User라는 이름으로 테이블 사용이 가능하다
@SQLDelete(sql = "UPDATED \"user\" SET deleted_at = NOW() where id = ?") // 삭제시 삭제전 발생
@Where(clause = "deleted_at is NULL") // SELECT시 deleted_at이 null인 경우만 조회되도록 where 절 추가
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_name")
    private String userName;
    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "register_at")
    private Timestamp registeredAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "deleted_at")
    private Timestamp deletedAt; // 회원 탈퇴시 flag가 아닌 실제 row가 삭제되는 경우 - 의도치 않은 삭제로 cs 문의시 삭제 log등 탐색 편리

    @PrePersist
    void registeredAt() {
        this.registeredAt = Timestamp.from(Instant.now());
    }
    @PreUpdate
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public static UserEntity of(String userName, String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userName);
        userEntity.setPassword(password);
        return userEntity;
    }

}
