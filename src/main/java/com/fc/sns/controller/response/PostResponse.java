package com.fc.sns.controller.response;

import com.fc.sns.model.Post;
import com.fc.sns.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@AllArgsConstructor
@Getter
public class PostResponse {
    private Integer id;

    private String title;
    private String body;
    private UserReponse user;
    private Timestamp registeredAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public static PostResponse fromPost(Post post) {

     return new PostResponse(
             post.getId(),
             post.getTitle(),
             post.getBody(),
             UserReponse.fromUser(post.getUser()),
             post.getRegisteredAt(),
             post.getUpdatedAt(),
             post.getDeletedAt()
     );
    }
}
