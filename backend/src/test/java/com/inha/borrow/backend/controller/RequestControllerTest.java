package com.inha.borrow.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.handler.GlobalErrorHandler;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.forAuthTest.admin.WithMockAdmin;
import com.inha.borrow.backend.forAuthTest.borrower.WithMockBorrower;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.entity.request.RequestManager;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.model.dto.request.SaveRequestResultDto;
import com.inha.borrow.backend.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
@Import({ AuthConfig.class, GlobalErrorHandler.class })
class RequestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private RequestService requestService;

        @MockitoBean
        private AdminAuthenticationProvider adminAuthenticationProvider;

        @MockitoBean
        private BorrowerAuthenticationProvider borrowerAuthenticationProvider;

        private SaveRequestDto saveRequestDto;
        private PatchRequestDto patchRequestDto;
        private Request request;

        private static final String TEST_USER_ID = "borrower1";

        @BeforeEach
        void setUp() {
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
                                .build();
                request = Request.builder()
                                .id(1)
                                .itemId(1)
                                .manager(RequestManager.builder().id("test").build())
                                .borrowerId(TEST_USER_ID)
                                .createdAt(Timestamp.from(Instant.now()))
                                .returnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(14)))
                                .borrowAt(Timestamp.from(Instant.now()))
                                .type(RequestType.BORROW)
                                .state(RequestState.PENDING)
                                .build();
        }

        @Test
        @DisplayName("리퀘스트 저장 성공")
        @WithMockBorrower
        void saveRequestSuccess() throws Exception {
                // given
                SaveRequestResultDto result = new SaveRequestResultDto(1, Timestamp.from(Instant.now()));
                given(requestService.saveRequest(any(SaveRequestDto.class))).willReturn(result);

                // when & then
                mockMvc.perform(post("/requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(saveRequestDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.createdAt").exists())
                                .andExpect(jsonPath("$.data.requestId").exists());
        }

        @ParameterizedTest(name = "invalid save body #{index}")
        @DisplayName("리퀘스트 저장 실패 - 유효성/역직렬화 오류")
        @WithMockBorrower
        @CsvSource(textBlock = """
                        '{}'
                        '{"itemId":"abc","returnAt":"2025-01-01T00:00:00","borrowerAt":"2025-01-01T00:00:00","type":"BORROW"}'
                        '{"itemId":1,"returnAt":"bad","borrowerAt":"2025-01-01T00:00:00","type":"BORROW"}'
                        '{"itemId":1,"returnAt":"2025-01-01T00:00:00","borrowerAt":"bad","type":"BORROW"}'
                        '{"itemId":1,"returnAt":"2025-01-01T00:00:00","borrowerAt":"2025-01-01T00:00:00","type":"INVALID"}'
                        """)
        void saveRequestFail_validation(String body) throws Exception {
                mockMvc.perform(post("/requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("리퀘스트 저장 실패 - 관리자 권한 403")
        @WithMockAdmin
        void saveRequestForbiddenForAdmin() throws Exception {
                mockMvc.perform(post("/requests")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(saveRequestDto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("리퀘스트 수정 성공")
        @WithMockBorrower
        void patchRequestSuccess() throws Exception {
                // given: 서비스 메서드가 void를 반환하도록 Mocking
                doNothing().when(requestService).patchRequest(any(PatchRequestDto.class), anyInt(), anyString());

                // when & then: PATCH 요청을 보내고 상태 검증
                mockMvc.perform(patch("/requests/{request-id}/patch", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequestDto)))
                                .andExpect(status().isOk());
        }

        @ParameterizedTest(name = "invalid patch body #{index}")
        @DisplayName("리퀘스트 수정 실패 - 유효성/역직렬화 오류")
        @WithMockBorrower
        @CsvSource(textBlock = """
                        '{}'
                        '{"returnAt":"bad","borrowerAt":"2025-01-01T00:00:00"}'
                        '{"returnAt":"2025-01-01T00:00:00","borrowerAt":"bad"}'
                        """)
        void patchRequestFail_validation(String body) throws Exception {
                mockMvc.perform(patch("/requests/{request-id}/patch", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("리퀘스트 수정 실패 - 관리자 권한 403")
        @WithMockAdmin
        void patchRequestForbiddenForAdmin() throws Exception {
                mockMvc.perform(patch("/requests/{request-id}/patch", 1)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequestDto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("리퀘스트 취소 성공")
        @WithMockBorrower
        void cancelRequestSuccess() throws Exception {
                doNothing().when(requestService).cancelRequest(anyInt(), anyString());

                mockMvc.perform(patch("/requests/{request-id}/cancel", 1)
                                .with(csrf()))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("리퀘스트 취소 실패 - 관리자 권한 403")
        @WithMockAdmin
        void cancelRequestForbiddenForAdmin() throws Exception {
                mockMvc.perform(patch("/requests/{request-id}/cancel", 1)
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ID로 리퀘스트 조회 성공")
        @WithMockBorrower
        void requestByIdSuccess() throws Exception {
                // given: 서비스 메서드가 Request 객체를 반환하도록 Mocking
                given(requestService.findById(any(User.class), anyInt())).willReturn(request);

                // when & then: GET 요청을 보내고 응답 검증
                mockMvc.perform(get("/requests/{request-id}", 1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(request.getId()))
                                .andExpect(jsonPath("$.data.borrowerId").value(request.getBorrowerId()));
        }

        @Test
        @DisplayName("ID로 리퀘스트 조회 성공 - 관리자")
        @WithMockAdmin
        void requestByIdSuccess_admin() throws Exception {
                given(requestService.findById(any(User.class), anyInt())).willReturn(request);
                mockMvc.perform(get("/requests/{request-id}", 1))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ID로 리퀘스트 조회 실패 - 비인증 401")
        void requestByIdUnauthorizedForAnonymous() throws Exception {
                mockMvc.perform(get("/requests/{request-id}", 1))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("조건으로 리퀘스트 목록 조회 성공 - 대여자")
        @WithMockBorrower
        void findDetailRequestSuccess() throws Exception {
                // given: 서비스 메서드가 리스트를 반환하도록 Mocking
                given(requestService.findByCondition(any(User.class), anyString(), anyString(), anyString()))
                                .willReturn(List.of(request));

                // when & then: GET 요청을 쿼리 파라미터와 함께 보내고 응답 검증
                mockMvc.perform(get("/requests")
                                .param("borrowerId", TEST_USER_ID)
                                .param("type", "BORROW")
                                .param("state", "PENDING"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.size()").value(1))
                                .andExpect(jsonPath("$.data[0].borrowerId").value(TEST_USER_ID));
        }

        @Test
        @DisplayName("조건으로 리퀘스트 목록 조회 성공 - 관리자")
        @WithMockAdmin
        void findDetailRequestSuccess_admin() throws Exception {
                given(requestService.findByCondition(any(User.class), anyString(), anyString(), anyString()))
                                .willReturn(List.of(request));
                mockMvc.perform(get("/requests")
                                .param("borrowerId", TEST_USER_ID)
                                .param("type", "BORROW")
                                .param("state", "PENDING"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("조건으로 리퀘스트 목록 조회 실패 - 비인증 401")
        void findDetailRequestUnauthorizedForAnonymous() throws Exception {
                mockMvc.perform(get("/requests")
                                .param("borrowerId", TEST_USER_ID)
                                .param("type", "BORROW")
                                .param("state", "PENDING"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("조건으로 리퀘스트 목록 조회 실패 - 미존재")
        @WithMockBorrower
        void findDetailRequestFail_notFound() throws Exception {
                doThrow(new ResourceNotFoundException(
                                ApiErrorCode.REQUEST_NOT_FOUND.name(), ApiErrorCode.REQUEST_NOT_FOUND.getMessage()))
                                .when(requestService)
                                .findByCondition(any(User.class), anyString(), anyString(), anyString());

                mockMvc.perform(get("/requests")
                                .param("borrowerId", TEST_USER_ID)
                                .param("type", "BORROW")
                                .param("state", "PENDING"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("대여요청 담당자 지정 성공")
        @WithMockAdmin
        void manageRequestSuccess() throws Exception {
                // given
                doNothing().when(requestService).manageRequest(anyString(), anyInt());

                // when & then
                mockMvc.perform(patch("/requests/{request-id}/manage", "1"))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("대여자 또는 일반사용자는 담당자 지정을 시도하면 403")
        @WithMockBorrower
        void manageRequestForbiddenForBorrower() throws Exception {
                mockMvc.perform(patch("/requests/{request-id}/manage", "1")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 요청에 대해 담당자 지정시 400 반환")
        @WithMockAdmin
        void manageRequestNotFound() throws Exception {
                org.mockito.Mockito.doThrow(new com.inha.borrow.backend.model.exception.ResourceNotFoundException(
                                ApiErrorCode.REQUEST_NOT_FOUND.name(),
                                ApiErrorCode.REQUEST_NOT_FOUND.getMessage()))
                                .when(requestService)
                                .manageRequest(anyString(), org.mockito.ArgumentMatchers.eq(999));

                mockMvc.perform(patch("/requests/{request-id}/manage", "999"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비인증 사용자는 담당자 지정을 시도하면 401")
        void manageRequestUnauthorizedForAnonymous() throws Exception {
                mockMvc.perform(patch("/requests/{request-id}/manage", "1"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("비인증 사용자는 저장/수정/취소 요청 시 401")
        void unauthorized_for_write_endpoints() throws Exception {
                mockMvc.perform(post("/requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(saveRequestDto)))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(patch("/requests/{request-id}/patch", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequestDto)))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(patch("/requests/{request-id}/cancel", 1))
                                .andExpect(status().isUnauthorized());
        }
}
