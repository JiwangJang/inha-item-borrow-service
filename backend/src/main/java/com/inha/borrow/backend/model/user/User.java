package com.inha.borrow.backend.model.user;

import java.util.List;

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
public abstract class User implements UserDetails {
    String id;
    String password;
    String email;
    String name;
    String phonenumber;
    List<GrantedAuthority> authorities;

    public String getUsername() {
        return this.id;
    }
}
