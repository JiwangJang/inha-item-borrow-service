package com.inha.borrow.backend.config.auth.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.entity.user.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인에 성공했을때 호출되는 클래스
 * <p>
 * 기본 동작은 '/'경로로 리다이렉팅인데 우리는 REST서비스이므로 리다이렉팅을 안하도록 변경
 * 
 * @author 장지왕
 */
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final Cache<String, CacheBorrowerDto> borrowerCache;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        User user = (User) authentication.getPrincipal();
        String authority = user.getAuthorities().get(0).getAuthority();

        if (authority.equals(Role.BORROWER.name())) {
            Borrower borrower = (Borrower) user;
            log.info("[INFO] 대여자 로그인 / 아이디 : {}", user.getId());
            CacheBorrowerDto cache = borrowerCache.getIfPresent(user.getId());
            if (cache == null) {
                cache = CacheBorrowerDto.builder()
                        .id(borrower.getId())
                        .name(borrower.getName())
                        .department(borrower.getDepartment())
                        .build();
            }

            response.getWriter()
                    .write(objectMapper.writeValueAsString(new ApiResponse<CacheBorrowerDto>(true, cache)));
            response.flushBuffer();
        } else {
            Admin admin = (Admin) user;
            response.getWriter()
                    .write(objectMapper.writeValueAsString(new ApiResponse<Admin>(true, admin)));
            response.flushBuffer();
            log.info("[INFO] 관리자 로그인 / 아이디 : {} / 권한 : {}", user.getId(), authority);
        }

        super.clearAuthenticationAttributes(request);
        return;
    }
}