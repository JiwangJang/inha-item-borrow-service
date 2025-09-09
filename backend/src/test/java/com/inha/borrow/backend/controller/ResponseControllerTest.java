package com.inha.borrow.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.enums.ResponseType;
import com.inha.borrow.backend.forAuthTest.admin.WithMockAdmin;
import com.inha.borrow.backend.forAuthTest.borrower.WithMockBorrower;
import com.inha.borrow.backend.model.dto.response.PatchResponseDto;
import com.inha.borrow.backend.model.dto.response.SaveResponseDto;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.service.ResponseService;

@WebMvcTest(controllers = ResponseController.class)
@Import(AuthConfig.class)
class ResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResponseService responseService;

    @MockitoBean
    AdminAuthenticationProvider mockAdminAuthenticationProvider;

    @MockitoBean
    BorrowerAuthenticationProvider mockBorrowerAuthenticationProvider;

    @Test
    @DisplayName("POST /responses 성공 - DIVISION_MEMBER")
    @WithMockAdmin
    void createResponse_success() throws Exception {
        // given
        SaveResponseDto req = new SaveResponseDto(1, null, ResponseType.BORROW);
        Response res = Response.builder()
                .id(10)
                .requestId(1)
                .createdAt(Timestamp.from(Instant.now()))
                .rejectReason(null)
                .type(ResponseType.BORROW)
                .build();
        when(responseService.createResponse(anyString(), any(SaveResponseDto.class))).thenReturn(res);

        // when - then
        mockMvc.perform(post("/responses")
                .content(objectMapper.writeValueAsString(req))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.requestId").value(1))
                .andExpect(jsonPath("$.data.type").value("BORROW"));
    }

    @Test
    @DisplayName("POST /responses 실패 - BORROWER는 403")
    @WithMockBorrower
    void createResponse_forbidden_for_borrower() throws Exception {
        SaveResponseDto req = new SaveResponseDto(1, null, ResponseType.BORROW);

        mockMvc.perform(post("/responses")
                .content(objectMapper.writeValueAsString(req))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /responses 실패 - 비로그인 401")
    void createResponse_unauthorized() throws Exception {
        SaveResponseDto req = new SaveResponseDto(1, null, ResponseType.BORROW);

        mockMvc.perform(post("/responses")
                .content(objectMapper.writeValueAsString(req))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /responses/{id} 성공 - DIVISION_MEMBER")
    @WithMockAdmin
    void updateResponse_success() throws Exception {
        PatchResponseDto patch = PatchResponseDto.builder()
                .requestId(1)
                .rejectReason("")
                .build();
        doNothing().when(responseService).updateResponse(anyString(), anyString(), any(PatchResponseDto.class));

        mockMvc.perform(patch("/responses/{response-id}", 10)
                .content(objectMapper.writeValueAsString(patch))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /responses/{id} 실패 - BORROWER는 403")
    @WithMockBorrower
    void updateResponse_forbidden_for_borrower() throws Exception {
        PatchResponseDto patch = PatchResponseDto.builder()
                .requestId(1)
                .rejectReason("")
                .build();

        mockMvc.perform(patch("/responses/{response-id}", 10)
                .content(objectMapper.writeValueAsString(patch))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /responses/{id} 실패 - 비로그인 401")
    void updateResponse_unauthorized() throws Exception {
        PatchResponseDto patch = PatchResponseDto.builder()
                .requestId(1)
                .rejectReason("")
                .build();

        mockMvc.perform(patch("/responses/{response-id}", 10)
                .content(objectMapper.writeValueAsString(patch))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isUnauthorized());
    }
}

