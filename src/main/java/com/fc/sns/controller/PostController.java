package com.fc.sns.controller;


import com.fc.sns.controller.request.PostCreateRequest;
import com.fc.sns.controller.request.PostModifyRequest;
import com.fc.sns.controller.response.PostResponse;
import com.fc.sns.controller.response.Response;
import com.fc.sns.model.Post;
import com.fc.sns.model.entity.PostEntity;
import com.fc.sns.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Response<Void> create(@RequestBody PostCreateRequest postCreateRequest, Authentication authentication) {
        postService.create(postCreateRequest.getTitle(), postCreateRequest.getBody(), authentication.getName());
        return Response.success(null);
    }

    @PutMapping("/{postId}")
    public Response<PostResponse> modify(@PathVariable Integer postId, @RequestBody PostModifyRequest postModifyRequest, Authentication authentication) {
        Post post = postService.modify(postModifyRequest.getTitle(), postModifyRequest.getBody(), authentication.getName(), postId);
        return Response.success(PostResponse.fromPost(post));
    }

    @DeleteMapping("/{postId}")
    public Response<Void> delete(@PathVariable Integer postId, Authentication authentication) {
        postService.delete(authentication.getName(), postId);
        return Response.success(null);
    }

}
