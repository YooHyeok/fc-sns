package com.fc.sns.service;

import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.model.Post;
import com.fc.sns.model.entity.PostEntity;
import com.fc.sns.model.entity.UserEntity;
import com.fc.sns.repository.PostEntityRepository;
import com.fc.sns.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;

    @Transactional
    public void create(String title, String body, String userName) {
        // user find
        UserEntity userEntity = userEntityRepository.findByUserName(userName)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));

        // post save
        postEntityRepository.save(PostEntity.of(title, body, userEntity));

    }

    @Transactional
    public Post modify(String title, String body, String userName, Integer postId) {
        UserEntity userEntity = userEntityRepository.findByUserName(userName)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));

        // post exist
        PostEntity postEntity = postEntityRepository.findById(postId)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not founded", postId)));

        // post permission
        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", userName, postId));
        }

        postEntity.setTitle(title);
        postEntity.setBody(body);
//        return Post.fromEntity(postEntityRepository.save(postEntity));
        /**
         * @PreUpdate를 통해 반영되는 entity를 다시 return
         * @PreUpdate로 저장되는 값은 db에 저장될때 반영이 되기때문에 영속성이 끝나야 반영된다.
         * 따라서 return되는 객체에는 그 값을 받아오기가 어려움
         * modify에서는 저장된 객체를 변환해서 return해주고 있다.
         * 이 객체에 정확한 값을 저장하기 위해 saveAndFlush를 이용하고 있다.
         *
         * 이러한 이유로 처음 업데이트시 updatedAt이 시각이 들어가지 않고 null이 들어가며 이 부분을 수정하기 위해 saveAndFlush로 변경한다.
         */
        return Post.fromEntity(postEntityRepository.saveAndFlush(postEntity));
    }
}
