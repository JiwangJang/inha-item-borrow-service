package com.inha.borrow.backend.enums;

public enum ApiErrorCode {
    EXIST_ID("이미 사용중인 아이디입니다."),
    INVALID_CODE("코드를 다시 확인해주세요."),
    NOT_ALLOWED_VALUE("허용되지 않는 값입니다."),
    DB_ERROR("데이터베이스 에러입니다."),
    SERVER_ERROR("서버쪽의 알 수 없는 에러입니다."),
    CHECK_YOUR_INFO("아이디나 비밀번호 확인하세요."),
    NOT_FOUND("요청하신 자원을 찾을수 없습니다."),
    JSON_PARSING_ERROR("JSON파싱중 에러가 발생했습니다.");

    private final String message;

    ApiErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
