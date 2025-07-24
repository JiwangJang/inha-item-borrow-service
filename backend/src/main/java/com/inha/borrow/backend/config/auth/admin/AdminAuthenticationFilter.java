package com.inha.borrow.backend.config.auth.admin;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.auth.handler.LoginSuccessHandler;
import com.inha.borrow.backend.config.auth.handler.LoginFailureHandler;
import com.inha.borrow.backend.model.auth.LoginRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 관리자 계정 인증을 실행하는 필터
 * UsernamePasswordAuthenticationFilter 상속
 * 
 * @author 장지왕
 */
public class AdminAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    ObjectMapper objectMapper = new ObjectMapper();

    public AdminAuthenticationFilter(AuthenticationManager authenticationManager) {
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
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/admins/login"));
    }

    /**
     * 인증이 실제로 진행되는 메서드
     * 여기서 인증 및 SecurityContextRepository 등록, 그 후 처리(리다이렉팅, 적절한 상태코드 반환 등)이 이루어짐
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            AdminAuthenticationToken authenticationToken = new AdminAuthenticationToken(loginRequest.getId(),
                    loginRequest.getPassword());
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (Exception e) {
            throw new AuthenticationServiceException("유효하지 않은 로그인 요청");
        }
    }
}
