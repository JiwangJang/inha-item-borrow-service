package com.inha.borrow.backend.config.auth.borrowers;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.inha.borrow.backend.model.entity.user.Borrower;

/**
 * 관리자 인증시 관리자 인증정보를 담는 객체
 * <p>
 * UsernamePasswordAuthenticationToken 상속
 * 
 * @author 장지왕
 */
public class BorrowerAuthenticationToken extends UsernamePasswordAuthenticationToken {
    /**
     * 인증되기 전의 UsernamePasswordAuthenticationToken을 만들때 사용하는 생성자
     * 
     * @param id
     * @param password
     */
    public BorrowerAuthenticationToken(String id, String password) {
        super(id, password);
    }

    /**
     * 인증된 UsernamePasswordAuthenticationToken을 만들때 사용하는 생성자
     * 
     * @param borrower
     * @param authorities
     */
    public BorrowerAuthenticationToken(Borrower borrower, List<GrantedAuthority> authorities) {
        super(borrower, null, authorities);
    }
}