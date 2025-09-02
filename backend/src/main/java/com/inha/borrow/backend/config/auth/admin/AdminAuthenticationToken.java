package com.inha.borrow.backend.config.auth.admin;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.inha.borrow.backend.model.entity.user.Admin;

/**
 * 관리자 인증시 관리자 인증정보를 담는 객체
 * <p>
 * UsernamePasswordAuthenticationToken 상속
 * 
 * @author 장지왕
 */
public class AdminAuthenticationToken extends UsernamePasswordAuthenticationToken {
    /**
     * 인증되기 전의 UsernamePasswordAuthenticationToken을 만들때 사용하는 생성자
     * 
     * @param id
     * @param password
     */
    public AdminAuthenticationToken(String id, String password) {
        super(id, password);
    }

    /**
     * 인증된 UsernamePasswordAuthenticationToken을 만들때 사용하는 생성자
     * 
     * @param admin
     * @param authorities
     */
    public AdminAuthenticationToken(Admin admin, List<GrantedAuthority> authorities) {
        super(admin, null, authorities);
    }

}