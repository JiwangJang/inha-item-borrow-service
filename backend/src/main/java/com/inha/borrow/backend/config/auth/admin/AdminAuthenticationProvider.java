package com.inha.borrow.backend.config.auth.admin;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.service.AdminService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 인증을 처리하는 클래스
 * <p>
 * AuthenticationProvider 구현
 * 
 * @author 장지왕
 */
@Component
@AllArgsConstructor
@Slf4j
public class AdminAuthenticationProvider implements AuthenticationProvider {
    private AdminService adminService;
    private PasswordEncoder passwordEncoder;
    private GrantedAuthoritiesMapper authoritiesMapper;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String id = (String) authentication.getPrincipal();
        String rawPassword = (String) authentication.getCredentials();

        Admin admin = (Admin) adminService.loadUserByUsername(id);
        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new BadCredentialsException("비밀번호 다름");
        }
        admin.setPassword(null);
        // Apply role hierarchy to expand authorities (e.g., PRESIDENT ⇒ DIVISION_HEAD ⇒ DIVISION_MEMBER)
        var mapped = authoritiesMapper == null ? admin.getAuthorities()
                : authoritiesMapper.mapAuthorities(admin.getAuthorities());
        return new AdminAuthenticationToken(admin, new java.util.ArrayList<GrantedAuthority>(mapped));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 이 프로바이더에 들어온 authentication을 처리할 수 있는지 없는지 표현하는 메서드
        // 여기서는 해당 클래스로 변환 가능한지 판단하는 메서드로 판단
        return AdminAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
