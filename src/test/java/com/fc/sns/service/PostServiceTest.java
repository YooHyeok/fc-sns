package com.fc.sns.service;

import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.fixture.PostEntityFixture;
import com.fc.sns.fixture.UserEntityFixture;
import com.fc.sns.model.entity.PostEntity;
import com.fc.sns.model.entity.UserEntity;
import com.fc.sns.repository.PostEntityRepository;
import com.fc.sns.repository.UserEntityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

@SpringBootTest
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @MockBean
    private PostEntityRepository postEntityRepository;
    @MockBean
    private UserEntityRepository userEntityRepository;

    @Test
    void 포스트작성이_성공한경우() throws Exception{
        String title = "title";
        String body = "body";
        String userName = "userName";

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(Mockito.mock(UserEntity.class)));
        Mockito.when(postEntityRepository.save(ArgumentMatchers.any())).thenReturn(Mockito.mock(PostEntity.class));

        Assertions.assertDoesNotThrow(() -> postService.create(title, body, userName));
    }

    @Test
    void 포스트작성시_요청한유저가_존재하지않는경우() throws Exception{
        String title = "title";
        String body = "body";
        String userName = "userName";

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());
        Mockito.when(postEntityRepository.save(ArgumentMatchers.any())).thenReturn(Optional.of(Mockito.mock(PostEntity.class)));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.create(title, body, userName));
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void 포스트수정이_성공한경우() throws Exception{
        String title = "title";
        String body = "body";
        String userName = "userName";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(userName, postId, 1);
        UserEntity userEntity = postEntity.getUser();

        //mocking
//        PostEntity mockPostEntity = Mockito.mock(PostEntity.class);
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(userEntity));
//        Mockito.when(postEntityRepository.findById(postId)).thenReturn(Optional.of(mockPostEntity));
        Mockito.when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity)); // Mock대신 Fixture적용
        Mockito.when(postEntityRepository.saveAndFlush(ArgumentMatchers.any())).thenReturn(postEntity);

        Assertions.assertDoesNotThrow(() -> postService.modify(title, body, userName, postId));
    }

    @Test
    void 포스트수정시_포스트가_존재하지않는_경우() throws Exception{
        String title = "title";
        String body = "body";
        String userName = "userName";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(userName, postId, 1);
        UserEntity userEntity = postEntity.getUser();

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(userEntity));
        Mockito.when(postEntityRepository.findById(postId)).thenReturn(Optional.empty());

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.modify(title, body, userName, postId));
        Assertions.assertEquals(ErrorCode.POST_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void 포스트수정시_권한이_없는_경우() throws Exception{
        String title = "title";
        String body = "body";
        String userName = "userName";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(userName, postId, 1);
        UserEntity writer = UserEntityFixture.get("userName1", "password", 2); // 다른 아이디를 넣어줌

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(writer));
        Mockito.when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.modify(title, body, userName, postId));
        Assertions.assertEquals(ErrorCode.INVALID_PERMISSION, e.getErrorCode());
    }

    @Test
    void 포스트삭제가_성공한경우() throws Exception{
        String userName = "userName";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(userName, postId, 1);
        UserEntity userEntity = postEntity.getUser();

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(userEntity));
        Mockito.when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity)); // Mock대신 Fixture적용

        Assertions.assertDoesNotThrow(() -> postService.delete(userName, postId));
    }

    @Test
    void 포스트삭제시_포스트가_존재하지않는_경우() throws Exception{
        String userName = "userName";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(userName, postId, 1);
        UserEntity userEntity = postEntity.getUser();

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(userEntity));
        Mockito.when(postEntityRepository.findById(postId)).thenReturn(Optional.empty());

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.delete(userName, postId));
        Assertions.assertEquals(ErrorCode.POST_NOT_FOUND, e.getErrorCode());
    }

    @Test
    void 포스트삭제시_권한이_없는_경우() throws Exception{
        String userName = "userName";
        Integer postId = 1;

        PostEntity postEntity = PostEntityFixture.get(userName, postId, 1);
        UserEntity writer = UserEntityFixture.get("userName1", "password", 2); // 다른 아이디를 넣어줌

        //mocking
        Mockito.when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(writer));
        Mockito.when(postEntityRepository.findById(postId)).thenReturn(Optional.of(postEntity));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> postService.delete(userName, postId));
        Assertions.assertEquals(ErrorCode.INVALID_PERMISSION, e.getErrorCode());
    }
}
