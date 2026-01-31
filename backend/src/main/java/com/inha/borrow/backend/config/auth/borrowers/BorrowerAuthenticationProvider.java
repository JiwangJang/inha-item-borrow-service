package com.inha.borrow.backend.config.auth.borrowers;

import com.inha.borrow.backend.model.dto.user.borrower.BorrowerLoginDto;
import com.inha.borrow.backend.service.BorrowerService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.inha.borrow.backend.model.entity.user.Borrower;

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
    private BorrowerService loginService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String id = (String) authentication.getPrincipal();
        String rawPassword = (String) authentication.getCredentials();
        BorrowerLoginDto borrowerLoginDto = new BorrowerLoginDto(id, rawPassword);

        Borrower borrower = loginService.inhaLogin(borrowerLoginDto);
        return new BorrowerAuthenticationToken(borrower, borrower.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 이 프로바이더에 들어온 authentication을 처리할 수 있는지 없는지 표현하는 메서드
        // 여기서는 해당 클래스로 변환 가능한지 판단하는 메서드로 판단
        return BorrowerAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
