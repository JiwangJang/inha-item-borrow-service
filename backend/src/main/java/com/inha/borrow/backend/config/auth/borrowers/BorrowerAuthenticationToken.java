package com.inha.borrow.backend.config.auth.borrowers;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * 관리자 인증시 관리자 인증정보를 담는 객체
 * <p>
 * UsernamePasswordAuthenticationToken 상속
 * 
 * @author 장지왕
 */
public class BorrowerAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public BorrowerAuthenticationToken(String id, String password) {
        super(id, password);
    }

    public BorrowerAuthenticationToken(String id, String password, List<GrantedAuthority> authorities) {
        super(id, null, authorities);
    }
}