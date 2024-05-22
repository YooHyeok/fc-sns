package com.fc.sns.model.entity;

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
@Table(name = "\"comment\""// postgreSQL은 User 테이블이 기본적으로 이미 존재하기 떄문에 ""를 붙혀야 User라는 이름으로 테이블 사용이 가능하다
, indexes = { //인덱스확인: select * from pg_indexes where tablename='comment'
    // 기본 index는 pk인 id 이지만, 댓글 조회시 postId를 기준으로 조회하기때문에 postId를 comment테이블의 index로 추가
    @Index(name = "post_id_idx", columnList = "post_id") // name:인덱스이름, columnList:인덱스로 추가할 실제 컬럼
} )
@SQLDelete(sql = "UPDATE \"comment\" SET deleted_at = NOW() where id = ?") // 삭제시 삭제전 발생
@Where(clause = "deleted_at is NULL") // SELECT시 deleted_at이 null인 경우만 조회되도록 where 절 추가
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PostEntity post;

    @Column(name = "comment")
    private String comment;
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

    public static CommentEntity of(UserEntity userEntity, PostEntity postEntity, String comment) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setUser(userEntity);
        commentEntity.setPost(postEntity);
        commentEntity.setComment(comment);
        return commentEntity;
    }

}
