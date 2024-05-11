package com.fc.sns.service;

import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
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
}
