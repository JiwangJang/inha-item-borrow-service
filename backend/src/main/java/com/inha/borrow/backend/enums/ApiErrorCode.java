package com.inha.borrow.backend.enums;

public enum ApiErrorCode {
    EXIST_ID("이미 사용중인 아이디입니다. 다른 아이디를 사용해주세요."),
    INVALID_ID("아이디 형식에 맞지 않습니다. 다른 아이디를 사용해주세요."),
    INVALID_PASSWORD("비밀번호 형식에 맞지 않습니다. 다른 비밀번호를 사용해주세요"),
    INVALID_VALUE(""),
    INCORRECT_PASSWORD("비밀번호가 다릅니다."),
    INCORRECT_CODE("일치하지 않는 인증코드입니다. 인증코드를 다시 확인해주세요."),
    SIGN_UP_SESSION_EXPIRED("회원가입세션이 만료됐습니다. 다시 처음부터 진행해주세요."),
    SIGN_UP_PASS_FAILED("회원가입에 필요한 절차를 모두 수행해주세요."),
    SMS_CODE_EXPIRED("인증시간이 만료된 코드입니다. 재발급후 인증하시기 바랍니다."),
    NOT_ALLOWED_VALUE("허용되지 않는 값입니다."),
    DB_ERROR("데이터베이스 에러입니다."),
    SERVER_ERROR("서버쪽의 알 수 없는 에러입니다. 계속되면 개발자에게 연락해주시기 바랍니다."),
    CHECK_YOUR_INFO("아이디나 비밀번호를 확인해주세요."),
    NOT_FOUND("요청하신 자원을 찾을수 없습니다."),
    JSON_PARSING_ERROR("JSON파싱중 에러가 발생했습니다."),
    FILE_SIZE_TOO_LARGE("사진의 크기가 너무 큽니다.");

    private String message;

    ApiErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
