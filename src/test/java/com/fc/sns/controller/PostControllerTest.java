package com.fc.sns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fc.sns.controller.request.PostCommentRequest;
import com.fc.sns.controller.request.PostCreateRequest;
import com.fc.sns.controller.request.PostModifyRequest;
import com.fc.sns.controller.request.UserJoinRequest;
import com.fc.sns.exception.ErrorCode;
import com.fc.sns.exception.SnsApplicationException;
import com.fc.sns.fixture.PostEntityFixture;
import com.fc.sns.model.Post;
import com.fc.sns.service.PostService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc // API 형태의 Controller Test
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService; // Controller에서 PostService를 의존주입 받기 때문에 실질적으로 사용하지 않더라도 모킹해줘야한다.

    @Test
    @WithMockUser // 인증된 회원
    void 포스트작성() throws Exception {
        String title = "title";
        String body = "body";
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    @WithAnonymousUser // 익명의 회원으로 요청을날림
    void 포스트작성시_로그인하지않은경우() throws Exception {
        String title = "title";
        String body = "body";

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized()); // 정상 동작
    }

    @Test
    @WithMockUser
    void 포스트수정() throws Exception {
        String title = "title";
        String body = "body";

        // 반환타입이 Void에서 변경되었으므로 mocking
        Mockito.when(postService.modify(ArgumentMatchers.eq(title), ArgumentMatchers.eq(body), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(Post.fromEntity(PostEntityFixture.get("userName", 1, 1)));

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/v1/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    @WithAnonymousUser // 익명의 회원으로 요청을날림
    void 포스트수정시_로그인하지않은경우() throws Exception {
        String title = "title";
        String body = "body";

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/v1/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 포스트수정시_본인이_작성한_글이_아니라면_에러발생() throws Exception {
        String title = "title";
        String body = "body";

        // mocking postService의 modify가 호출될때 Error throw
        Mockito.doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION))
                .when(postService)
                .modify(ArgumentMatchers.eq(title), ArgumentMatchers.eq(body), ArgumentMatchers.any(), ArgumentMatchers.eq(1));

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/v1/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                //TODO : add request body
                                .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 포스트수정시_수정하려는_글이_없는경우_에러발생() throws Exception {
        String title = "title";
        String body = "body";

        // mocking
        Mockito.doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND))
                .when(postService)
                .modify(ArgumentMatchers.eq(title), ArgumentMatchers.eq(body), ArgumentMatchers.any(), ArgumentMatchers.eq(1));

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/v1/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))//add request body
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser
    void 포스트삭제() throws Exception {

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    @WithAnonymousUser
    void 포스트삭제시_로그인하지_않은경우() throws Exception {

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 포스트삭제시_작성자와_삭제요청자가_다를경우() throws Exception {

        // mocking
        Mockito.doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService).delete(ArgumentMatchers.any(), ArgumentMatchers.any());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 포스트삭제시_삭제하려는_포스트가_존재하지_않을경우() throws Exception {

        // mocking
        Mockito.doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).delete(ArgumentMatchers.any(), ArgumentMatchers.any());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/posts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser
    void 피드목록() throws Exception{

        // TODO: mocking
        Mockito.when(postService.list(ArgumentMatchers.any())).thenReturn(Page.empty());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    @WithAnonymousUser
    void 피드목록요청시_로그인하지_않은경우() throws Exception {

        // TODO: mocking
        Mockito.when(postService.list(ArgumentMatchers.any())).thenReturn(Page.empty());

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 내피드목록() throws Exception{

        // TODO: mocking
        Mockito.when(postService.my(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Page.empty());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/posts/my")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    @WithAnonymousUser
    void 내피드목록요청시_로그인하지_않은경우() throws Exception {

        // TODO: mocking
        Mockito.when(postService.my(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Page.empty());

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/posts/my")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 좋아요기능() throws Exception{

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/posts/1/likes")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    @WithAnonymousUser
    void 좋아요버튼클릭시_로그인하지_않은경우() throws Exception {

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/posts/1/likes")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 좋아요버튼클릭시_게시물이_없는경우() throws Exception {
        Mockito.doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).like(ArgumentMatchers.any(), ArgumentMatchers.any());

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/posts/1/likes")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser
    void 댓글기능() throws Exception{

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/posts/1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))//add request body
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isOk()); // 정상 동작
    }

    @Test
    @WithAnonymousUser
    void 댓글작성시_로그인하지_않은경우() throws Exception {

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/posts/1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))//add request body
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 댓글작성시_게시물이_없는경우() throws Exception {
        Mockito.doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).comment(ArgumentMatchers.any(), ArgumentMatchers.any());

        //로그인 하지 않은 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/posts/1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))//add request body
                ).andDo(MockMvcResultHandlers.print()) // 출력
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
