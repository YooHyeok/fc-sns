package com.fc.sns.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor // 단일필드일 경우 역직렬화시 기본생성자가 필수로 필요하다. https://github.com/FasterXML/jackson-databind/issues/1498
public class PostCommentRequest {
    private String comment;
}
