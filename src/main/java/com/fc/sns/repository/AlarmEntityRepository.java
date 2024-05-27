package com.fc.sns.repository;

import com.fc.sns.model.entity.AlarmEntity;
import com.fc.sns.model.entity.LikeEntity;
import com.fc.sns.model.entity.PostEntity;
import com.fc.sns.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AlarmEntityRepository extends JpaRepository<AlarmEntity, Integer> {

    Page<AlarmEntity> findAllByUser(UserEntity userEntity, Pageable pageable);

    Page<AlarmEntity> findAllByUserId(Integer userId, Pageable pageable);

}
