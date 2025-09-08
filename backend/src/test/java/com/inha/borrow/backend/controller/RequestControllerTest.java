package com.inha.borrow.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;

import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
@Import(AuthConfig.class)
class RequestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private RequestService requestService;


        private SaveRequestDto saveRequestDto;
        private PatchRequestDto patchRequestDto;
        private Request findRequest;
        private User testUser;

        private static final String TEST_USER_ID = "borrower1";

        @BeforeEach
        void setUp() {
                // Mock User 객체 생성 및 권한 설정
                List<GrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + Role.BORROWER.name()));
                testUser = new User(
                                TEST_USER_ID, "password", "email@test.com", "테스트유저", "010-1234-5678", "refreshToken",
                                authorities) {
                        // 추상 클래스인 User를 직접 인스턴스화할 수 없으므로 익명 클래스로 구현
                        @Override
                        public boolean isAccountNonExpired() {
                                return true;
                        }

                        @Override
                        public boolean isAccountNonLocked() {
                                return true;
                        }

                        @Override
                        public boolean isCredentialsNonExpired() {
                                return true;
                        }

                        @Override
                        public boolean isEnabled() {
                                return true;
                        }
                };

                saveRequestDto = SaveRequestDto.builder()
                                .itemId(1)
                                .borrowerId(TEST_USER_ID)
                                .returnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                                .borrowerAt(Timestamp.valueOf(LocalDateTime.now()))
                                .type(RequestType.BORROW)
                                .build();

                patchRequestDto = PatchRequestDto.builder()
                                .returnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                                .borrowerAt(Timestamp.valueOf(LocalDateTime.now()))
                                .type(RequestType.BORROW)
                                .build();

                findRequest = new Request(
                                1,
                                1,
                                TEST_USER_ID,
                                Timestamp.valueOf(LocalDateTime.now()),
                                Timestamp.valueOf(LocalDateTime.now().plusDays(7)),
                                Timestamp.valueOf(LocalDateTime.now()),
                                RequestType.BORROW,
                                RequestState.PENDING,
                                false);

        }

        @Test
        @DisplayName("리퀘스트 저장 성공")
        void saveRequestSuccess() throws Exception {
                // given: 서비스 메서드가 정수 ID를 반환하도록 Mocking
                given(requestService.saveRequest(any(User.class), any(SaveRequestDto.class), anyInt()))
                                .willReturn(1);

                // when & then: POST 요청을 보내고 응답 검증
                mockMvc.perform(post("/requests")
                                .with(csrf())
                                .with(user(testUser))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(saveRequestDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").value(1));
        }

        @Test
        @DisplayName("리퀘스트 수정 성공")
        void patchRequestSuccess() throws Exception {
                // given: 서비스 메서드가 void를 반환하도록 Mocking
                doNothing().when(requestService).patchRequest(any(PatchRequestDto.class), anyInt(), anyString());

                // when & then: PATCH 요청을 보내고 상태 검증
                mockMvc.perform(patch("/requests/{request-id}/patch", 1)
                                .with(csrf())
                                .with(user(TEST_USER_ID).authorities(new SimpleGrantedAuthority("ROLE_BORROWER")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequestDto)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("리퀘스트 취소 성공")
        void cancelRequestSuccess() throws Exception {
                doNothing().when(requestService).cancelRequest(anyInt(), anyString());

                mockMvc.perform(patch("/requests/{request-id}/cancel", 1)
                                .with(csrf())
                                .with(user(TEST_USER_ID).authorities(new SimpleGrantedAuthority("ROLE_BORROWER"))))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("리퀘스트 상태 변경 성공")
        void evaluateRequestSuccess() throws Exception {
                doNothing().when(requestService).evaluationRequest(any(RequestState.class), anyInt());

                mockMvc.perform(patch("/requests/{request-id}/evaluate", 1)
                                .with(csrf())
                                .with(user(TEST_USER_ID)
                                                .authorities(new SimpleGrantedAuthority("ROLE_DIVISION_MEMBER")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(RequestState.PERMIT)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ID로 리퀘스트 조회 성공")
        void findRequestByIdSuccess() throws Exception {
                // given: 서비스 메서드가 Request 객체를 반환하도록 Mocking
                given(requestService.findById(any(User.class), anyInt())).willReturn(findRequest);

                // when & then: GET 요청을 보내고 응답 검증
                mockMvc.perform(get("/requests/{request-id}", 1)
                                .with(user(testUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(findRequest.getId()))
                                .andExpect(jsonPath("$.data.borrowerId").value(findRequest.getBorrowerId()));
        }

        @Test
        @DisplayName("조건으로 리퀘스트 목록 조회 성공")
        void findDetailRequestSuccess() throws Exception {
                // given: 서비스 메서드가 리스트를 반환하도록 Mocking
                given(requestService.findByCondition(any(User.class), anyString(), anyString(), anyString()))
                                .willReturn(List.of(findRequest));

                // when & then: GET 요청을 쿼리 파라미터와 함께 보내고 응답 검증
                mockMvc.perform(get("/requests")
                                .with(user(testUser))
                                .param("borrowerId", TEST_USER_ID)
                                .param("type", "BORROW")
                                .param("state", "PENDING"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.size()").value(1))
                                .andExpect(jsonPath("$.data[0].borrowerId").value(TEST_USER_ID));
        }
}