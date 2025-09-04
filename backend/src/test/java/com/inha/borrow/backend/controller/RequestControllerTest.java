package com.inha.borrow.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(RequestController.class)
@WithMockUser
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestService requestService;

    private SaveRequestDto saveRequestDto;
    private Request request;
    private PatchRequestDto patchRequestDto;

    @BeforeEach
    void setUp() {
        // DB에 저장될 때 ID가 자동 생성되므로, SaveRequest 객체에는 ID를 포함하지 않습니다.
        saveRequestDto = SaveRequestDto.builder()
                .itemId(1)
                .borrowerId("borrower1")
                .returnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                .borrowerAt(Timestamp.valueOf(LocalDateTime.now()))
                .type(RequestType.BORROW)
                .build();

        // DB에서 조회될 FindRequest 객체는 ID를 포함합니다.
        request = new Request(
                1,
                1,
                "borrower1",
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now().plusDays(7)),
                Timestamp.valueOf(LocalDateTime.now()),
                RequestType.BORROW,
                RequestState.PENDING,
                false);

        patchRequestDto = new PatchRequestDto(
                Timestamp.valueOf(LocalDateTime.now().plusDays(14)),
                Timestamp.valueOf(LocalDateTime.now()),
                RequestType.BORROW
        );
    }

    @Test
    @DisplayName("리퀘스트 저장 성공")
    void saveRequestSuccess() throws Exception {
        given(requestService.saveRequest(any(SaveRequestDto.class), eq(saveRequestDto.getItemId())))
                .willReturn(saveRequestDto);

        mockMvc.perform(post("/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemId").value(saveRequestDto.getItemId()))
                .andExpect(jsonPath("$.data.borrowerId").value(saveRequestDto.getBorrowerId()));
    }

    @Test
    @DisplayName("리퀘스트 수정 성공")
    void patchRequestSuccess() throws Exception {
        String borrowerId = "borrower1";
        int requestId = 1;

        doNothing().when(requestService).patchRequest(any(PatchRequestDto.class), eq(requestId), eq(borrowerId));

        mockMvc.perform(patch("/requests/{request-id}/patch", requestId)
                        .with(csrf())
                        .with(user(borrowerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리퀘스트 취소 성공")
    void cancelRequestSuccess() throws Exception {
        String borrowerId = "borrower1";
        int requestId = 1;

        doNothing().when(requestService).cancelRequest(eq(requestId), eq(borrowerId));

        mockMvc.perform(patch("/requests/{request-id}/cancel", requestId)
                        .with(csrf())
                        .with(user(borrowerId)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리퀘스트 상태 변경 성공")
    void evaluateRequestSuccess() throws Exception {
        int requestId = 1;
        RequestState newState = RequestState.PERMIT;

        doNothing().when(requestService).evaluationRequest(eq(newState), eq(requestId));

        mockMvc.perform(patch("/requests/{request-id}/evaluate", requestId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newState)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ID로 리퀘스트 조회 성공")
    void findRequestByIdSuccess() throws Exception {
        int requestId = 1;
        given(requestService.findById(eq(requestId))).willReturn(request);

        mockMvc.perform(get("/requests/{request-id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(requestId))
                .andExpect(jsonPath("$.data.borrowerId").value(request.getBorrowerId()));
    }

    @Test
    @DisplayName("여러 조건으로 리퀘스트 목록 조회 성공")
    void findDetailRequestSuccess() throws Exception {
        String borrowerId = "borrower1";
        String type = "BORROW";
        String state = "PENDING";

        List<Request> expectedRequests = Collections.singletonList(request);
        given(requestService.findByCondition(eq(borrowerId), eq(type), eq(state)))
                .willReturn(expectedRequests);

        mockMvc.perform(get("/requests/detailrequest") // 쿼리 파라미터만 사용하므로 URL 경로를 변경
                        .param("borrowerId", borrowerId)
                        .param("type", type)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.size()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(request.getId()));
    }

    // findRequestUserSuccess 메서드 수정
    // 실패함;;;
    // 누가 해줘
    @Test
    @DisplayName("사용자가 자신이 요청한 리퀘스트 목록 조회 성공")
    void findRequestUserSuccess() throws Exception {
        String borrowerId = "borrower1";

        // Mocking: 서비스가 한 개의 객체를 포함한 리스트를 반환하도록 설정
        when(requestService.findRequestUser(anyString())).thenReturn(Collections.singletonList(request));

        // 요청 URI를 컨트롤러의 @GetMapping 경로인 "/requests"로 수정
        mockMvc.perform(get("/requests/requestuser")
                        .with(user(borrowerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.size()").value(1))
                .andExpect(jsonPath("$.data[0].borrowerId").value(borrowerId));
    }
}