package com.inha.borrow.backend.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.forAuthTest.admin.WithMockAdmin;
import com.inha.borrow.backend.forAuthTest.borrower.WithMockBorrower;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.UpdateStudentCouncilFeeVerificationDenyDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.FindFeeVerificationRequestDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.ModifyVerificationResponseDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.PermitFeeVerificationDto;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.service.StudentCouncilFeeVerificationService;

@WebMvcTest(controllers = StuentCouncilFeeVerificationController.class)
@Import(AuthConfig.class)
public class StudentCouncilFeeVerificationControllerTest {
    // 인증하는거 없어서 안돌아감 -> 나중에 인증 하는거 완료되고 돌려봐야함
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentCouncilFeeVerificationService service;

    @MockitoBean
    AdminAuthenticationProvider mockAdminAuthenticationProvider;

    @MockitoBean
    MockMultipartFile image;

    @MockitoBean
    BorrowerAuthenticationProvider mockBorrowerAuthenticationProvider;

    // --------GET /student-council-fee-verification------------
    @Test
    @DisplayName("GET /student-council-fee-verification | 전체 목록 조회 메서드(성공)")
    @WithMockAdmin
    void findAllRequestsTestSuccess() throws Exception {
        // given
        List<StudentCouncilFeeVerification> expectedResults = new ArrayList<>();
        when(service.findAllRequests()).thenReturn(expectedResults);

        // when - then
        mockMvc.perform(get("/student-council-fee-verification"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /student-council-fee-verification | 전체 목록 조회 메서드(실패-대여자 권한)")
    @WithMockBorrower
    void findAllRequestsTestFailForBorrowerAuth() throws Exception {
        // given
        List<StudentCouncilFeeVerification> expectedResults = new ArrayList<>();
        when(service.findAllRequests()).thenReturn(expectedResults);

        // when - then
        mockMvc.perform(get("/student-council-fee-verification"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /student-council-fee-verification | 전체 목록 조회 메서드(실패-로그인 안함)")
    @WithMockUser
    void findAllRequestsTestFailForAnonymous() throws Exception {
        // given
        // when - then
        mockMvc.perform(get("/student-council-fee-verification"))
                .andExpect(status().isUnauthorized());
    }

    // --------GET /student-council-fee-verification/single------------
    @Test
    @DisplayName("GET /student-council-fee-verification/single | 단건 조회 메서드(성공)")
    @WithMockAdmin
    void findRequestsByIdTestSuccessForAdmin() throws Exception {
        // given
        Admin admin = Admin.builder().build();
        FindFeeVerificationRequestDto dto = new FindFeeVerificationRequestDto("null");
        StudentCouncilFeeVerification expectedResult = new StudentCouncilFeeVerification();
        when(service.findRequestById(admin, dto)).thenReturn(expectedResult);

        // when - then
        mockMvc.perform(get("/student-council-fee-verification/single")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /student-council-fee-verification/single | 전체 목록 조회 메서드(성공-대여자 권한)")
    @WithMockBorrower
    void findRequestsByIdTestSuccessForBorrower() throws Exception {
        // given
        Borrower borrower = Borrower
                .builder()
                .id("1111111")
                .build();
        StudentCouncilFeeVerification expectedResult = new StudentCouncilFeeVerification();
        when(service.findRequestById(borrower, null)).thenReturn(expectedResult);

        // when - then
        mockMvc.perform(get("/student-council-fee-verification/single"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /student-council-fee-verification/single | 전체 목록 조회 메서드(실패-로그인 안함)")
    @WithMockUser
    void findRequestsByIdTestFailForAnonymous() throws Exception {
        // given
        // when - then
        mockMvc.perform(get("/student-council-fee-verification/single"))
                .andExpect(status().isUnauthorized());
    }

    // --------PATCH /student-council-fee-verification/permit------------
    @Test
    @DisplayName("PATCH /student-council-fee-verification/permit | 요청 승인 메서드(성공)")
    @WithMockAdmin
    void permitVerificationTestSuccessForAdmin() throws Exception {
        // given
        doNothing().when(service).permitVerificationRequest("test");
        PermitFeeVerificationDto dto = new PermitFeeVerificationDto("test");

        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/permit")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /student-council-fee-verification/permit | 요청 승인 메서드(실패-대여자 권한)")
    @WithMockBorrower
    void permitVerificationTestFailForBorrower() throws Exception {
        // given
        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/permit"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /student-council-fee-verification/permit | 요청 승인 메서드(실패-로그인 안함)")
    @WithMockUser
    void permitVerificationTestFailForAnonymous() throws Exception {
        // given
        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/permit"))
                .andExpect(status().isUnauthorized());
    }

    // --------PATCH /student-council-fee-verification/deny------------
    @Test
    @DisplayName("PATCH /student-council-fee-verification/deny | 요청 승인 메서드(성공)")
    @WithMockAdmin
    void denyVerificationTestSuccessForAdmin() throws Exception {
        // given
        UpdateStudentCouncilFeeVerificationDenyDto dto = new UpdateStudentCouncilFeeVerificationDenyDto("test",
                "reason");
        doNothing().when(service).denyVerificationRequest(dto);

        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/deny")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /student-council-fee-verification/deny | 요청 승인 메서드(실패-대여자 권한)")
    @WithMockBorrower
    void denyVerificationTestFailForBorrower() throws Exception {
        // given
        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/deny"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /student-council-fee-verification/deny | 요청 승인 메서드(실패-로그인 안함)")
    @WithMockUser
    void denyVerificationTestFailForAnonymous() throws Exception {
        // given
        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/deny"))
                .andExpect(status().isUnauthorized());
    }

    // --------PATCH /student-council-fee-verification/modify------------
    @Test
    @DisplayName("PATCH /student-council-fee-verification/modify | 응답 수정 메서드(성공)")
    @WithMockAdmin
    void reviseVerificationResponseTestSuccessForAdmin() throws Exception {
        // given
        ModifyVerificationResponseDto dto = new ModifyVerificationResponseDto("test", false, "reason");
        doNothing().when(service).modifyVerificationResponse(dto);

        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/modify")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /student-council-fee-verification/modify | 응답 수정 메서드(실패-대여자 권한)")
    @WithMockBorrower
    void reviseVerificationResponseTestFailForBorrower() throws Exception {
        // given
        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/modify"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /student-council-fee-verification/modify | 응답 수정 메서드(실패-로그인 안함)")
    @WithMockUser
    void reviseVerificationResponseTestFailForAnonymous() throws Exception {
        // given
        // when - then
        mockMvc.perform(patch("/student-council-fee-verification/modify"))
                .andExpect(status().isUnauthorized());
    }

    // -------- POST /student-council-fee-verification------------
    // 관리자 권한은 따로 테스트 안함(관리자가 굳이 포스트맨 이용해서 업무를 망치진 않을것이기에)
    @Test
    @DisplayName("POST /student-council-fee-verification | 요청 등록 메서드(성공-대여자 권한)")
    @WithMockBorrower
    void requestVerificationTestFailForBorrower() throws Exception {
        // given
        // when - then
        mockMvc.perform(multipart("/student-council-fee-verification")
                .file(image)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /student-council-fee-verification | 요청 등록 메서드(실패-로그인 안함)")
    @WithMockUser
    void requestVerificationTestFailForAnonymous() throws Exception {
        // given
        // when - then
        mockMvc.perform(post("/student-council-fee-verification"))
                .andExpect(status().isUnauthorized());
    }

    // -------- DELETE /student-council-fee-verification------------
    // 관리자 권한은 따로 테스트 안함(관리자가 굳이 포스트맨 이용해서 업무를 망치진 않을것이기에)
    @Test
    @DisplayName("DELETE /student-council-fee-verification | 요청 취소 메서드(성공-대여자 권한)")
    @WithMockBorrower
    void cancelVerificationTestFailForBorrower() throws Exception {
        // given
        // when - then
        mockMvc.perform(delete("/student-council-fee-verification"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /student-council-fee-verification | 요청 취소 메서드(실패-로그인 안함)")
    @WithMockUser
    void cancelVerificationTestFailForAnonymous() throws Exception {
        // given
        // when - then
        mockMvc.perform(delete("/student-council-fee-verification"))
                .andExpect(status().isUnauthorized());
    }

}
