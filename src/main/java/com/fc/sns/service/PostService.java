package com.fc.sns.service;

import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.model.AlarmArgs;
import com.fc.sns.model.AlarmType;
import com.fc.sns.model.Comment;
import com.fc.sns.model.Post;
import com.fc.sns.model.entity.*;
import com.fc.sns.model.event.AlarmEvent;
import com.fc.sns.producer.AlarmProducer;
import com.fc.sns.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final CommentEntityRepository commentEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final AlarmService alarmService;
    private final AlarmProducer alarmProducer;


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

    @Transactional
    public void delete(String userName, Integer postId) {
        UserEntity userEntity = getUserOrException(userName);
        PostEntity postEntity = getPostOrException(postId);

        // post permission
        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", userName, postId));
        }

        likeEntityRepository.deleteAllByPost(postEntity);
        commentEntityRepository.deleteAllByPost(postEntity);
        postEntityRepository.delete(postEntity);
    }

    public Page<Post> list(Pageable pageable) {
        return postEntityRepository.findAll(pageable).map(Post::fromEntity);
    }

    public Page<Post> my(String userName, Pageable pageable) {
        UserEntity userEntity = getUserOrException(userName);
        return postEntityRepository.findAllByUser(userEntity, pageable).map(Post::fromEntity);
    }

    @Transactional
    public void like(Integer postId, String userName) {
        UserEntity userEntity = getUserOrException(userName);
        PostEntity postEntity = getPostOrException(postId);

        // check liked -> throw (이미 좋아요 누른경우 throw)
        likeEntityRepository.findByUserAndPost(userEntity, postEntity).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.ALREADY_LIKED, String.format("userName %s already like post %d", userName, postId));
        });

        likeEntityRepository.save(LikeEntity.of(userEntity, postEntity));

        /* SSE - Kafka 적용 */
        alarmProducer.send(
                new AlarmEvent(
                        postEntity.getUser().getId(),
                        AlarmType.NEW_LIKE_ON_POST,
                        new AlarmArgs(userEntity.getId(), postEntity.getId())
                )
        );

        /* SSE 적용 */
        /*AlarmEntity alarmEntity = alarmEntityRepository.save(
                AlarmEntity.of(
                        postEntity.getUser(), // post 작성자 (알람 수신 대상자)
                        AlarmType.NEW_LIKE_ON_POST, //알람 타입
                        new AlarmArgs(userEntity.getId(), postEntity.getId()) // 알람 고유정보 - comment 작성자, 게시글 번호
                )
        );
        alarmService.send(alarmEntity.getId(), postEntity.getUser().getId());*/
    }

    public Integer likeCount(Integer postId) {
        PostEntity postEntity = getPostOrException(postId);
        return likeEntityRepository.countByPost(postEntity);
    }

    @Transactional
    public void comment(Integer postId, String userName, String comment) {
        UserEntity userEntity = getUserOrException(userName);
        PostEntity postEntity = getPostOrException(postId);

        commentEntityRepository.save(CommentEntity.of(userEntity, postEntity, comment));

        /* SSE - Kafka 적용 */
        alarmProducer.send(
                new AlarmEvent(
                        postEntity.getUser().getId(),
                        AlarmType.NEW_COMMENT_ON_POST,
                        new AlarmArgs(userEntity.getId(), postEntity.getId())
                )
        );

        /* SSE 적용 */
        /*AlarmEntity alarmEntity = alarmEntityRepository.save(
                AlarmEntity.of(
                        postEntity.getUser(), // post 작성자 (알람 수신 대상자)
                        AlarmType.NEW_COMMENT_ON_POST, //알람 타입
                        new AlarmArgs(userEntity.getId(), postEntity.getId()) // 알람 고유정보 - comment 작성자, 게시글 번호
                )
        );
        alarmService.send(alarmEntity.getId(), postEntity.getUser().getId());*/

    }
    public Page<Comment> getComments(Integer postId, Pageable pageable) {
        PostEntity postEntity = getPostOrException(postId);
        return commentEntityRepository.findAllByPost(postEntity, pageable).map(Comment::fromEntity);
    }

    private PostEntity getPostOrException(Integer postId) {
        return postEntityRepository.findById(postId)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not founded", postId)));
    }

    private UserEntity getUserOrException(String userName) {
        return userEntityRepository.findByUserName(userName)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));
    }

}
