package com.fc.sns.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> {
    private String resultCode;
    private T result;

    /**
     * result 가 null 이므로 Generic은 Void로 지정한다.
     * @param errorCode
     * @return
     */
    public static Response<Void> error(String errorCode) {
        return new Response<>(errorCode, null);
    }

    /**
     * 성공시 결과 null 및 단순 성공 문자만 반환하는 Void 형태
     * @return
     */
    public static Response<Void> success() {
        return new Response<Void>("SUCCESS", null);
    }

    /**
     * 성공시 결과와 성공문자를 함께 반환하는 형태
     * @param result
     * @return
     * @param <T>
     */
    public static <T> Response<T> success(T result) {
        return new Response<>("SUCCESS", result);
    }

    public String toStream() {
        if (result == null) {
            return "{" +
                    "\"resultCode\":" + "\"" + resultCode + "\"," +
                    "\"result\":" + "\"" + null + "\"" + "}";
        }
        return "{" +
                "\"resultCode\":" + "\"" + resultCode + "\"," +
                "\"result\":" + "\"" + result + "\"" + "}";
    }
}
