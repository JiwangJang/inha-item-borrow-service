package com.inha.borrow.backend.forAuthTest.admin;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationToken;
import com.inha.borrow.backend.model.entity.user.Admin;

public class WithMockAdminSecurityContextFactory implements WithSecurityContextFactory<WithMockAdmin> {
    @Override
    public SecurityContext createSecurityContext(WithMockAdmin customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(customUser.role().name()));
        Admin admin = Admin.builder()
                .id(customUser.id())
                .password("")
                .email(customUser.id() + "@test.com")
                .name("테스트관리자")
                .phonenumber("010-0000-0000")
                .authorities(authorities)
                .divisionCode(customUser.division())
                .build();

        context.setAuthentication(new AdminAuthenticationToken(admin, authorities));
        return context;
    }
}
