package com.inha.borrow.backend.config.auth.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.response.ApiResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인에 실패했을때 실행되는 클래스
 * <p>
 * 실패했을 경우 400을 반환함
 */
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.getWriter()
                .write(objectMapper.writeValueAsString(new ApiResponse<>(false, ApiErrorCode.CHECK_YOUR_INFO.name())));
        response.flushBuffer();
        return;
    }
}
