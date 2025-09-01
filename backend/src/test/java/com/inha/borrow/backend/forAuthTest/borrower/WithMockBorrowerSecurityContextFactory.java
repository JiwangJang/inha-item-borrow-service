package com.inha.borrow.backend.forAuthTest.borrower;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationToken;
import com.inha.borrow.backend.enums.Role;

public class WithMockBorrowerSecurityContextFactory
                implements WithSecurityContextFactory<WithMockBorrower> {
        @Override
        public SecurityContext createSecurityContext(WithMockBorrower customUser) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                Authentication auth = BorrowerAuthenticationToken.authenticated("test_borrower", null,
                                List.of(new SimpleGrantedAuthority(Role.BORROWER.name())));
                context.setAuthentication(auth);
                return context;
        }
}
