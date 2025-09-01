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
    public AdminAuthenticationToken(String id, String password) {
        super(id, password);
    }

    public AdminAuthenticationToken(Admin admin, String password, List<GrantedAuthority> authorities) {
        super(admin, null, authorities);
    }

}