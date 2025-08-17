package com.inha.borrow.backend.controller.intergration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.forAuthTest.borrower.WithMockBorrower;
import com.inha.borrow.backend.model.dto.user.PatchPasswordDto;
import com.inha.borrow.backend.service.BorrowerService;

/**
 * 권한 테스트만 진행함(장지왕), 세부 기능테스트는 따로 진행하여야 함
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
public class BorrowerControllerTest {
        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @Autowired
        BorrowerService borrowerService;

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
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info 경로 접근(실패-대여자권한 아님)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void findByIdFailForAdminTest() throws Exception {
                mockMvc.perform(get("/borrowers/info"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info/password 경로접근 테스트(성공)")
        @WithMockBorrower
        void patchPasswordSuccessTest() throws Exception {
                // given
                PatchPasswordDto passwordDto = new PatchPasswordDto();
                passwordDto.setOriginPassword("examPass1!");
                passwordDto.setNewPassword("examPass1!");
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/password")
                                                .content(objectMapper.writeValueAsString(passwordDto))
                                                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/borrowers/info/password 경로접근 테스트(실패-인증없음)")
        @WithAnonymousUser
        void patchPasswordFailForAnonymousUserTest() throws Exception {
                // given
                PatchPasswordDto passwordDto = new PatchPasswordDto();
                passwordDto.setOriginPassword("1234");
                passwordDto.setNewPassword("1234");
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/password")
                                                .content(objectMapper.writeValueAsString(passwordDto))
                                                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info/password 경로접근 테스트(실패-권한문제)")
        @WithMockUser(authorities = "PRESIDENT")
        void patchPasswordFailForAdminTest() throws Exception {
                // given
                PatchPasswordDto passwordDto = new PatchPasswordDto();
                passwordDto.setOriginPassword("1234");
                passwordDto.setNewPassword("1234");
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/password")
                                                .content(objectMapper.writeValueAsString(passwordDto))
                                                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info/email 경로접근 테스트(성공)")
        @WithMockBorrower
        void patchEmailSuccessTest() throws Exception {
                // given
                String email = "test1@naver.com";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/email")
                                                .content(email))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/borrowers/info/email 경로접근 테스트(실패-인증없음)")
        @WithAnonymousUser
        void patchEmailFailForAnonymousUserTest() throws Exception {
                // given
                String email = "test1@naver.com";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/email")
                                                .content(email))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info/email 경로접근 테스트(실패-권한문제)")
        @WithMockUser(authorities = "PRESIDENT")
        void patchEmailFailForAdminTest() throws Exception {
                // given
                String email = "test1@naver.com";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/email")
                                                .content(email))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info/phonenum 경로접근 테스트(성공)")
        @WithMockBorrower
        void patchPhoneNumberSuccessTest() throws Exception {
                // given
                String phoneNumber = "010-0000-0000";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/phonenum")
                                                .content(phoneNumber))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/borrowers/info/phoneNumber 경로접근 테스트(실패-인증없음)")
        @WithAnonymousUser
        void patchPhoneNumberFailForAnonymousUserTest() throws Exception {
                // given
                String phoneNumber = "010-0000-0000";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/email")
                                                .content(phoneNumber))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/info/phoneNumber 경로접근 테스트(실패-권한문제)")
        @WithMockUser(authorities = "PRESIDENT")
        void patchPhoneNumberFailForAdminTest() throws Exception {
                // given
                String phoneNumber = "010-0000-0000";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/info/email")
                                                .content(phoneNumber))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/{borrower-id}/info/name 경로접근 테스트(성공)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void patchNameSuccessTest() throws Exception {
                // given
                String name = "수정함";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/test_borrower/info/name")
                                                .content(name))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/borrowers/{borrower-id}/info/name 경로접근 테스트(실패-권한문제)")
        @WithMockBorrower
        void patchNameFailForBorrowerTest() throws Exception {
                // given
                String name = "수정함";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/test_borrower/info/name")
                                                .content(name))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/borrowers/{borrower-id}/info/name 경로접근 테스트(실패-인증안함)")
        @WithAnonymousUser
        void patchNameFailForAnonymousUserTest() throws Exception {
                // given
                String name = "수정함";
                // when
                // then
                mockMvc.perform(
                                patch("/borrowers/test_borrower/info/name")
                                                .content(name))
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
                                .andExpect(status().isForbidden());
        }
}
