package com.inha.borrow.backend.model.auth;

import lombok.Data;

/**
 * 대여자의 핸드폰인증 코드를 잠시 저장하는 클래스
 * 
 * @author 장지왕
 */
@Data
public class SMSCode {
    String code;
    long ttl;

    public SMSCode(String code) {
        this.code = code;
        // 3분의 유효기간을 준다
        this.ttl = System.currentTimeMillis() + 180000;
    }

    /**
     * 테스트용 생성자
     * 
     * @param code
     * @param ttl
     */
    public SMSCode(String code, Long ttl) {
        this.code = code;
        this.ttl = ttl;
    }
}
