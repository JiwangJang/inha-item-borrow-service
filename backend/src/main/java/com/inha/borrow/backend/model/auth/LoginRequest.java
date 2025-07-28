package com.inha.borrow.backend.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 대여자, 관리자 로그인시 JSON요청을 파싱하기 위한 클래스
 * 
 * @author 장지왕
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    String id;
    String password;
}
