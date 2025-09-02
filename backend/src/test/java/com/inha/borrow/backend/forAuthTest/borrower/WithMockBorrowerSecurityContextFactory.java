package com.inha.borrow.backend.forAuthTest.borrower;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationToken;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.entity.user.Borrower;

public class WithMockBorrowerSecurityContextFactory
                implements WithSecurityContextFactory<WithMockBorrower> {
        @Override
        public SecurityContext createSecurityContext(WithMockBorrower customUser) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Role.BORROWER.name()));
                Borrower borrower = Borrower.builder()
                                .id(customUser.id())
                                .password("")
                                .email(customUser.id() + "@test.com")
                                .name("테스트 대여자")
                                .phonenumber("010-0000-0000")
                                .authorities(authorities)
                                .refreshToken("rt-" + customUser.id())
                                .ban(false)
                                .withDrawal(false)
                                .studentNumber("11111111")
                                .accountNumber("111111111")
                                .build();
                Authentication auth = BorrowerAuthenticationToken.authenticated(borrower, null,
                                authorities);
                context.setAuthentication(auth);
                return context;
        }
}
