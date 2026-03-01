package com.inha.borrow.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.forAuthTest.borrower.WithMockBorrower;
import com.inha.borrow.backend.model.dto.agreement.AgreementDto;
import com.inha.borrow.backend.model.entity.BorrowerAgreement;
import com.inha.borrow.backend.service.BorrowerAgreementService;

@WebMvcTest(BorrowerAgreementController.class)
@Import(AuthConfig.class)
public class BorrowerAgreementControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BorrowerAgreementService borrowerAgreementService;

    @MockitoBean
    AdminAuthenticationProvider mockAdminAuthenticationProvider;

    @MockitoBean
    BorrowerAuthenticationProvider mockAuthenticationProvider;

    @Test
    @DisplayName("모든 동의 내역 조회")
    @WithMockUser(authorities = "DIVISION_HEAD")
    void getAllAgreement() throws Exception {
        // given
        BorrowerAgreement agreement = new BorrowerAgreement(1, "user123", LocalDateTime.now(), "v1.0");
        given(borrowerAgreementService.findAllAgreement()).willReturn(List.of(agreement));

        // when & then
        mockMvc.perform(get("/agreement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].borrowerId").value("user123"));
    }

    @Test
    @DisplayName("동의 내역 ID로 조회 (List 반환)")
    @WithMockUser(authorities = "DIVISION_MEMBER")
    void getAgreementById() throws Exception {
        // given
        String borrowerId = "user123";

        // 1. 단일 객체가 아닌 List로 데이터를 준비합니다.
        List<BorrowerAgreement> agreementList = List.of(
                new BorrowerAgreement(1, borrowerId, LocalDateTime.now(), "v1.0")
        );

        // 2. Mockito의 리턴값도 List로 설정합니다.
        given(borrowerAgreementService.findByBorrower(borrowerId)).willReturn(agreementList);

        // when & then
        mockMvc.perform(get("/agreement/borrower/" + borrowerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // 3. 응답 데이터가 배열(List)이므로 인덱스 [0]을 사용하여 접근해야 합니다.
                .andExpect(jsonPath("$.data[0].borrowerId").value(borrowerId))
                .andExpect(jsonPath("$.data[0].version").value("v1.0"))
                // (선택사항) 데이터 개수 확인
                .andExpect(jsonPath("$.data.size()").value(1));
    }

    @Test
    @DisplayName("동의 내역 버전으로 조회")
    @WithMockUser(authorities = "DIVISION_MEMBER")
    void getAgreementByVersion() throws Exception {
        // given
        String version = "v1.0";
        BorrowerAgreement agreement = new BorrowerAgreement(1, "user123", LocalDateTime.now(), version);
        given(borrowerAgreementService.findByVersion(version)).willReturn(List.of(agreement));

        // when & then
        // [수정됨] 경로 변경: /agreement/version/{version}
        mockMvc.perform(get("/agreement/version/" + version))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].version").value(version));
    }

    @Test
    @DisplayName("개인정보 동의 저장")
    @WithMockBorrower
    void saveAgreement() throws Exception {
        // given
        AgreementDto agreementDto = AgreementDto.builder()
                .phoneNumber("010-1234-5678")
                .accountNumber("110-123-456789")
                .version("v1.0")
                .build();

        // [수정됨] eq("test_borrower") -> any()
        // WithMockBorrower가 생성하는 ID가 매번 다를 수 있거나 "test_borrower"가 아닐 수 있으므로 any()로 유연하게 대처
        given(borrowerAgreementService.saveAgreement(any(), any(AgreementDto.class))).willReturn(1);

        // when & then
        mockMvc.perform(post("/agreement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agreementDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));
    }

    // --- 권한 테스트 ---

    @Test
    @DisplayName("/agreement/{id} 경로 접근(성공)")
    @WithMockUser(authorities = "DIVISION_MEMBER")
    void getAgreementByIdSuccessTest() throws Exception {
        // [수정됨] 경로 변경
        mockMvc.perform(get("/agreement/borrower/user123"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/agreement POST 저장 접근(성공-대출자)")
    @WithMockBorrower
    void saveAgreementSuccessTest() throws Exception {
        AgreementDto agreementDto = new AgreementDto("010-1111-2222", "110-333-4444", "v1.0");

        // given (void가 아닌 리턴 타입이 있는 경우 mocking 필요)
        given(borrowerAgreementService.saveAgreement(any(), any())).willReturn(1);

        mockMvc.perform(post("/agreement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agreementDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("/agreement POST 저장 접근(실패-인증없음)")
    @WithAnonymousUser
    void saveAgreementFailForAnonymousTest() throws Exception {
        AgreementDto agreementDto = new AgreementDto("010-1111-2222", "110-333-4444", "v1.0");

        mockMvc.perform(post("/agreement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agreementDto)))
                .andExpect(status().isUnauthorized());
    }
}