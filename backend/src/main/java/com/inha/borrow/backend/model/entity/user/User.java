package com.inha.borrow.backend.model.entity.user;

import java.util.List;

import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * UserDetails를 구현하는 추상클래스
 * 
 * @author 장지왕
 */
@Data
@AllArgsConstructor
@SuperBuilder
public abstract class User implements UserDetails {
    private String id;
    private String password;
    private String email;
    private String name;
    private String phonenumber;
    private String refreshToken;
    private List<GrantedAuthority> authorities;

    public String getUsername() {
        return this.id;
    }
}
