package com.inha.borrow.backend.config.auth.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.entity.user.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        if (authentication != null) {
            // 로그인 한 유저가 경로에 접근한 경우
            User user = (User) authentication.getPrincipal();
            String userId = user.getId();
            String role = user.getAuthorities().get(0).getAuthority();

            log.info("[INFO] {}({}) 로그아웃", userId, role);
        }

        ApiResponse<String> apiResponse = new ApiResponse<>(true, "로그아웃 되었습니다.");
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
