package com.inha.borrow.backend.config.auth.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.apiResponse.ErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 로그인에 실패했을때 실행되는 클래스
 * <p>
 * 실패했을 경우 400을 반환함
 */
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
        private final ObjectMapper objectMapper;

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException exception) throws IOException, ServletException {
                ErrorResponse errorResponse = new ErrorResponse();

                if (exception.getMessage().equals(ApiErrorCode.INVALID_ID.name())) {
                        errorResponse.setErrorCode(ApiErrorCode.INVALID_ID.name());
                        errorResponse.setErrorMessage("미래융합대학 학생만 이용가능합니다.");
                } else {
                        errorResponse.setErrorCode(ApiErrorCode.CHECK_YOUR_INFO.name());
                        errorResponse.setErrorMessage(ApiErrorCode.CHECK_YOUR_INFO.getMessage());
                }
                ApiResponse<ErrorResponse> apiResponse = new ApiResponse<>(false, errorResponse);

                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getWriter()
                                .write(objectMapper.writeValueAsString(apiResponse));
                response.flushBuffer();
                return;
        }
}