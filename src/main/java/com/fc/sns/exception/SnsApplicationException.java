package com.fc.sns.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SnsApplicationException extends RuntimeException {
    private ErrorCode errorCode;
    private String message;

    /**
     * 에러 코드만 받고 나머지는 null로 처리할 경우
     * ex) password 오류에서 password정보를 알릴 필요는 없..
     * @param errorCode
     */
    public SnsApplicationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = null;
    }

    @Override
    public String getMessage() {
        if (message == null) return errorCode.getMessage();
        return String.format("%s. %s", errorCode.getMessage(), message);
    }
}
