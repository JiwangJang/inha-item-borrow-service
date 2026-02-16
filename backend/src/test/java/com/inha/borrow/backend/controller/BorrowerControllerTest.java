package com.inha.borrow.backend.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.inha.borrow.backend.model.dto.user.borrower.SavePhoneAccountNumberDto;
import org.apache.http.entity.ContentType;
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
import com.inha.borrow.backend.model.dto.user.borrower.PatchPhonenumberDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.service.BorrowerService;

@WebMvcTest(BorrowerController.class)
@Import(AuthConfig.class)
public class BorrowerControllerTest {
        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @MockitoBean
        BorrowerService borrowerService;

        @MockitoBean
        AdminAuthenticationProvider mockAdminAuthenticationProvider;

        @MockitoBean
        BorrowerAuthenticationProvider mockAuthenticationProvider;

        // 인자 테스트
        @Test
        @DisplayName("모든 대여자 조회")
        @WithMockUser(authorities = "DIVISION_HEAD")
        void findAllBorrower() throws Exception {
                Borrower borrower = Borrower.builder().id("123").name("홍길동").build();
                given(borrowerService.findAll()).willReturn(List.of(borrower));

                mockMvc.perform(get("/borrowers"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].id").value("123"))
                                .andExpect(jsonPath("$.data[0].name").value("홍길동"));
        }

        @Test
        @DisplayName("대여자 id로 조회")
        @WithMockBorrower
        void findById() throws Exception {
                Borrower borrower = Borrower.builder()
                                .id("test_borrower")
                                .name("홍길동")
                                .build();
                when(borrowerService.findById(anyString())).thenReturn(borrower);

                mockMvc.perform(get("/borrowers/info"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value("test_borrower"))
                                .andExpect(jsonPath("$.data.name").value("홍길동"));
        }

        @Test
        @DisplayName("이름 수정")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void patchName() throws Exception {
                doNothing().when(borrowerService).patchName("123", "새이름");

                mockMvc.perform(patch("/borrowers/123/info/name")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("\"새이름\""))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("전화번호 및 계좌번호 수정 요청 성공")
        void patchUserNumber_Success() throws Exception {
                // given
                String borrowerId = "user123";
                SavePhoneAccountNumberDto dto = new SavePhoneAccountNumberDto("010-1234-5678", "110-123-456789");

                // when & then
                mockMvc.perform(patch("/{borrower-id}/info/account-num", borrowerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isOk());

                // 서비스 메서드가 올바른 파라미터로 호출되었는지 검증
                verify(borrowerService).savePhoneAccountNumber(eq(borrowerId), any(SavePhoneAccountNumberDto.class));
        }

        @Test
        @DisplayName("계좌번호 수정")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void patchAccountNumber() throws Exception {
                doNothing().when(borrowerService).patchAccountNumber("123", "새이름");

                mockMvc.perform(patch("/borrowers/123/info/name")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("\"새이름\""))
                        .andExpect(status().isOk());
        }

        @Test
        @DisplayName("전화번호 수정")
        @WithMockBorrower
        void patchPhoneNumber() throws Exception {
                PatchPhonenumberDto dto = new PatchPhonenumberDto("010-1111-2222");
                doNothing().when(borrowerService).patchPhoneNumber("testId", dto);

                mockMvc.perform(patch("/borrowers/info/phonenum")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Ban 상태 수정")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void patchBan() throws Exception {
                doNothing().when(borrowerService).patchBan(true, "123");

                mockMvc.perform(patch("/borrowers/123/info/ban")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("true"))
                                .andExpect(status().isOk());
        }
        // 이하 권한 테스트

        @Test
        @DisplayName("/borrowers 경로 접근은 국장급(DIVISION_HEAD)이상만 가능")
        @WithMockUser(authorities = "DIVISION_HEAD")
        void findAllBorrowerSuccessTest() throws Exception {
                mockMvc.perform(get("/borrowers"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/borrowers 경로 접근(실패-권한문제)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void findAllBorrowerFailTest() throws Exception {
                mockMvc.perform(get("/borrowers"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info 경로 접근(성공)")
        @WithMockBorrower
        void findByIdSuccessTest() throws Exception {
                mockMvc.perform(get("/borrowers/info"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/borrowers/info 경로 접근(실패-인증없음)")
        @WithAnonymousUser
        void findByIdFailForAnonymousUserTest() throws Exception {
                mockMvc.perform(get("/borrowers/info"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("/borrowers/info 경로 접근(실패-대여자권한 아님)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void findByIdFailForAdminTest() throws Exception {
                mockMvc.perform(get("/borrowers/info"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info/phonenum 경로접근 테스트(성공)")
        @WithMockBorrower
        void patchPhoneNumberSuccessTest() throws Exception {
                PatchPhonenumberDto dto = new PatchPhonenumberDto("010-0000-0000");
                doNothing().when(borrowerService).patchPhoneNumber("test_borrower", dto);

                mockMvc.perform(
                                patch("/borrowers/info/phonenum")
                                                .content(objectMapper.writeValueAsString(dto))
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/borrowers/info/phonenum 경로접근 테스트(실패-인증없음)")
        @WithAnonymousUser
        void patchPhoneNumberFailForAnonymousUserTest() throws Exception {
                PatchPhonenumberDto dto = new PatchPhonenumberDto("010-0000-0000");
                mockMvc.perform(
                                patch("/borrowers/info/phonenum")
                                                .content(objectMapper.writeValueAsString(dto))
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("/borrowers/info/phonenum 경로접근 테스트(실패-권한문제)")
        @WithMockUser(authorities = "PRESIDENT")
        void patchPhoneNumberFailForAdminTest() throws Exception {
                PatchPhonenumberDto dto = new PatchPhonenumberDto("010-0000-0000");
                mockMvc.perform(
                                patch("/borrowers/info/phonenum")
                                                .content(objectMapper.writeValueAsString(dto))
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/{borrower-id}/info/ban 경로접근 테스트(성공)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void patchBanSuccessTest() throws Exception {
                // given
                boolean ban = true;
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/test_borrower/info/ban")
                                                .content(String.valueOf(ban)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/borrowers/{borrower-id}/info/ban 경로접근 테스트(실패-권한문제)")
        @WithMockBorrower
        void patchBanFailForBorrowerTest() throws Exception {
                // given
                boolean ban = true;
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/test_borrower/info/ban")
                                                .content(String.valueOf(ban)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/{borrower-id}/info/ban 경로접근 테스트(실패-인증안함)")
        @WithAnonymousUser
        void patchBanFailForAnonymousUserTest() throws Exception {
                // given
                boolean ban = true;
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/test_borrower/info/ban")
                                                .content(String.valueOf(ban)))
                                .andExpect(status().isUnauthorized());
        }
}
