package com.inha.borrow.backend.model.auth;

import com.inha.borrow.backend.util.ServiceUtils;

import lombok.Data;

/**
 * 대여자가 회원가입을 할 때 인증상태를 임시로 저장하는 객체
 * 
 * @author 장지왕
 */
@Data
public class SignUpSession {
    boolean idCheck = true;
    boolean passwordCheck = false;
    boolean phoneCheck = false;
    long ttl;

    public SignUpSession() {
        // 10분후 무효가 됨
        ttl = ServiceUtils.getTtl();
    }

    /*
     * 테스트용 생성자
     */
    public SignUpSession(Long ttl) {
        // 10분후 무효가 됨
        this.ttl = ttl;
    }
}
