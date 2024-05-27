package com.fc.sns.repository;

import com.fc.sns.model.entity.CommentEntity;
import com.fc.sns.model.entity.LikeEntity;
import com.fc.sns.model.entity.PostEntity;
import com.fc.sns.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CommentEntityRepository extends JpaRepository<CommentEntity, Integer> {

    Page<CommentEntity> findAllByPost(PostEntity postEntity, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE LikeEntity entity SET entity.deletedAt = now() WHERE entity.post= :postEntity")
    void deleteAllByPost(PostEntity postEntity);
}
