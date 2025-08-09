package com.inha.borrow.backend.config.auth.borrowers;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.server.ServerErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.auth.handler.LoginSuccessHandler;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.config.auth.handler.LoginFailureHandler;
import com.inha.borrow.backend.model.auth.LoginRequestDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 대여자 계정 인증을 실행하는 필터
 * UsernamePasswordAuthenticationFilter 상속
 * 
 * @author 장지왕
 */
public class BorrowerAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    ObjectMapper objectMapper = new ObjectMapper();

    public BorrowerAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
        // 세션을 구현하기 위해 설정
        // 기본값은 RequestAttributeSecurityContextRepository로 인증 성공시 세션유지 안됨
        super.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        // 인증 성공시 실행될 동작 등록
        setAuthenticationSuccessHandler(new LoginSuccessHandler());
        // 인증 실패시 실행될 동작 등록
        setAuthenticationFailureHandler(new LoginFailureHandler());
        // 특정경로만 처리하도록 설정
        setRequiresAuthenticationRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/borrowers/auth/login"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            LoginRequestDto loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            BorrowerAuthenticationToken authenticationToken = new BorrowerAuthenticationToken(loginRequest.getId(),
                    loginRequest.getPassword());
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            throw new ServerErrorException(ApiErrorCode.JSON_PARSING_ERROR.name(), e);
        }
    }
}
