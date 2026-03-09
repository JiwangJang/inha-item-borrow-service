package com.inha.borrow.backend.config.auth.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerCacheData;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.service.ItemService;
import com.inha.borrow.backend.service.RequestService;

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
    private final Cache<String, BorrowerCacheData> borrowerCache;
    private final ObjectMapper objectMapper;
    private final ItemService itemService;
    private final RequestService requestService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        User user = (User) authentication.getPrincipal();
        String authority = user.getAuthorities().get(0).getAuthority();
        Map<String, Object> initialValue = new HashMap<>();

        if (authority.equals(Role.BORROWER.name())) {
            Borrower borrower = (Borrower) user;
            log.info("[INFO] 대여자 로그인 / 아이디 : {}", user.getId());
            BorrowerCacheData borrowerCacheData = borrowerCache.getIfPresent(user.getId());
            if (borrowerCacheData == null) {
                borrowerCacheData = BorrowerCacheData.builder()
                        .id(borrower.getId())
                        .name(borrower.getName())
                        .department(borrower.getDepartment())
                        .build();
            }

            // 초기값 내려주는 작업
            List<Request> requests = requestService.findRequestsByCondition(user, null, null, null);
            List<Item> items = itemService.getAllItem(user);

            initialValue.put("borrowerInfo", borrowerCacheData);
            initialValue.put("requests", requests);
            initialValue.put("items", items);

            ApiResponse<Map<String, Object>> res = new ApiResponse<>(true, initialValue);

            response.getWriter()
                    .write(objectMapper.writeValueAsString(res));
            response.flushBuffer();
        } else {
            log.info("[INFO] 관리자 로그인 / 아이디 : {} / 권한 : {}", user.getId(), authority);
        }

        super.clearAuthenticationAttributes(request);
        return;
    }
}