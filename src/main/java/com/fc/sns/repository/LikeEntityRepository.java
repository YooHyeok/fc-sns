package com.fc.sns.repository;

import com.fc.sns.model.Post;
import com.fc.sns.model.entity.LikeEntity;
import com.fc.sns.model.entity.PostEntity;
import com.fc.sns.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LikeEntityRepository extends JpaRepository<LikeEntity, Integer> {

    Optional<LikeEntity> findByUserAndPost(UserEntity userEntity, PostEntity postEntity);
    List<LikeEntity> findAllByPost(PostEntity postEntity);

    @Query(value = "SELECT COUNT(*) FROM LikeEntity entity WHERE entity.post = :postEntity")
    Integer countByPost(PostEntity postEntity);


    /**
     * 일반적으로 JPA에서 지원하는 쿼리메소드(사용자정의)로 Delete나 Update를 하게되면
     * JPA의 기본 원리상 영속성 컨텍스트에 의해 관리되어야 하므로
     * 해당 데이터를 조회해 와서 영속성 컨텍스트에 저장(스냅샷)한다.
     * 따라서 일반적인 delete나 update를 하기 위해서는 JPQL을 활용하여 직접적인 Query를 날린다.
     */
    @Modifying
    @Query(value = "UPDATE CommentEntity entity SET entity.deletedAt = now() WHERE entity.post= :post")
    void deleteAllByPost(PostEntity post);
}
