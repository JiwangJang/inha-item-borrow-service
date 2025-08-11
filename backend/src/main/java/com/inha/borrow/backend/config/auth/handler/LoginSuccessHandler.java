package com.inha.borrow.backend.config.auth.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인에 성공했을때 호출되는 클래스
 * <p>
 * 기본 동작은 '/'경로로 리다이렉팅인데 우리는 REST서비스이므로 리다이렉팅을 안하도록 변경
 * 
 * @author 장지왕
 */
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        super.clearAuthenticationAttributes(request);
        return;
    }
}