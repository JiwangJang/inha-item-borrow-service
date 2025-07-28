package com.inha.borrow.backend.enums;

public enum ApiErrorCode {
    NOT_ALLOWED_VALUE("허용되지 않는 값입니다."),
    DB_ERROR("데이터베이스 에러입니다."),
    SERVER_ERROR("서버쪽의 알 수 없는 에러입니다."),
    NOT_FOUND("요청하신 자원을 찾을수 없습니다.");

    private final String message;

    ApiErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
