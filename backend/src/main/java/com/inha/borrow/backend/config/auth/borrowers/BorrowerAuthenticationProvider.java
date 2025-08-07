package com.inha.borrow.backend.config.auth.borrowers;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.inha.borrow.backend.model.user.Borrower;
import com.inha.borrow.backend.service.BorrowerService;

import lombok.AllArgsConstructor;

/**
 * 대여자 인증을 처리하는 클래스
 * <p>
 * AuthenticationProvider 구현
 * 
 * @author 장지왕
 */
@Component
@AllArgsConstructor
public class BorrowerAuthenticationProvider implements AuthenticationProvider {
    private BorrowerService borrowerService;
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String id = (String) authentication.getPrincipal();
        String rawPassword = (String) authentication.getCredentials();

        Borrower borrower = (Borrower) borrowerService.loadUserByUsername(id);
        if (!passwordEncoder.matches(rawPassword, borrower.getPassword())) {
            throw new BadCredentialsException("");
        }
        return new BorrowerAuthenticationToken(id, null, borrower.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 이 프로바이더에 들어온 authentication을 처리할 수 있는지 없는지 표현하는 메서드
        // 여기서는 해당 클래스로 변환 가능한지 판단하는 메서드로 판단
        return BorrowerAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
