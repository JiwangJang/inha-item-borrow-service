package com.inha.borrow.backend.enums;

public enum ApiErrorCode {
    EXIST_ID("이미 사용중인 아이디입니다. 다른 아이디를 사용해주세요."),
    INVALID_ID("아이디 형식에 맞지 않습니다. 다른 아이디를 사용해주세요."),
    INVALID_PASSWORD("비밀번호 형식에 맞지 않습니다. 다른 비밀번호를 사용해주세요"),
    INVALID_DIVISION_C0DE("이미 존재하는 부서 코드입니다."),
    INVALID_VALUE("잘못된 값입니다."),
    INVALID_RESPONSE_TYPE("요청의 타입과 응답의 타입은 같아야합니다."),
    NOT_ALLOWED_RESPONSE_TYPE("반납요청에 대한 응답만 수정이 가능합니다"),
    ALREADY_RESPONEDED_REQUEST("이미 처리된 요청입니다."),
    INVALID_ITEM_ID("현재 빌릴수 없는 물품입니다."),
    INVALID_REQUEST_ID("이전 요청이 수락되어야 반납이 가능합니다."),
    CAN_NOT_MODIFY_REQUEST("담당자 배정 전의 요청만 수정 가능합니다."),
    CAN_NOT_CANCEL_REQUEST("대여요청만 취소 가능합니다."),
    INCORRECT_PASSWORD("비밀번호가 다릅니다."),
    INCORRECT_CODE("일치하지 않는 인증코드입니다. 인증코드를 다시 확인해주세요."),
    NOT_ALLOWED_VALUE("허용되지 않는 값입니다."),
    DB_ERROR("데이터베이스 에러입니다."),
    SERVER_ERROR("서버쪽의 알 수 없는 에러입니다. 계속되면 개발자에게 연락해주시기 바랍니다."),
    CHECK_YOUR_INFO("아이디나 비밀번호를 확인해주세요."),
    ITEM_STATUS_NOT_AFFORD("물품의 상태가 AFFORD가 아닙니다"),
    NOT_FOUND("요청하신 자원을 찾을수 없습니다."),
    NOT_FOUND_DIVISION("존재하지 않는 부서입니다."),
    NOT_FOUND_ADMIN("존재하지 않는 관리자계정입니다."),
    NOT_FOUND_BORROWER("존재하지 않는 대여자입니다."),
    NOT_FOUND_ITEM("존재하지 않는 물품입니다."),
    NOT_FOUND_REQUEST(" 존재하지 않는 대여요청입니다."),
    NOT_FOUND_RESPONSE(" 존재하지 않는 대여(반납)응답입니다."),
    NOT_FOUND_NOTICE(" 존재하지 않는 공지입니다."),
    NOT_FOUND_AGREEMENT(" 존재하지 않는 동의정보입니다."),
    NOT_FOUND_CACHE("캐쉬가 존재하지 않습니다."),
    NOT_FOUND_COUNCIL("학생회비 납부확인 요청이 존재하지 않습니다."),
    JSON_PARSING_ERROR("JSON파싱중 에러가 발생했습니다."),
    FILE_SIZE_TOO_LARGE("사진의 크기가 너무 큽니다."),
    REQUIRED_FILE_MISSING("필수 사진이 누락되었습니다."),
    NOT_ALLOWED("요청하신 작업을 할 권한이 없습니다."),
    NOT_ALLOWED_COUNCIL_FEE("요청하신 작업을 할 권한이 없습니다."),
    NOT_LOGINED("로그인 후 이용가능합니다.");

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
