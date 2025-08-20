package com.inha.borrow.backend.controller.unit;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.cache.IdCache;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.controller.SignUpRequestController;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.SignUpFormDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.service.S3Service;
import com.inha.borrow.backend.service.SignUpRequestService;

@WebMvcTest(controllers = SignUpRequestController.class)
@Import(AuthConfig.class)
public class SignUpRequestControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private SignUpRequestService signUpRequestService;

        @MockitoBean
        private S3Service s3Service;

        @MockitoBean
        private IdCache idCache;

        @MockitoBean
        AdminAuthenticationProvider mockAdminAuthenticationProvider;

        @MockitoBean
        BorrowerAuthenticationProvider mockAuthenticationProvider;

        @Test
        @DisplayName("전체 회원가입 신청서 조회(성공-국원 이상만 접근가능)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void findAllSignUpRequestSuccessTest() throws Exception {
                SignUpForm form = new SignUpForm("null", "null", "null", "null", "null", "null", "null", " ");
                when(signUpRequestService.findSignUpRequest()).thenReturn(List.of(form));
                mockMvc.perform(get("/borrowers/signup-requests"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("대여자 권한으로 전체 회원가입 신청서 조회(실패-국원 이상만 접근가능)")
        @WithMockUser(authorities = "BORROWER")
        void findAllSignUpRequestFailForAuthroityTest() throws Exception {
                SignUpForm form = new SignUpForm("null", "null", "null", "null", "null", "null", "null", " ");
                when(signUpRequestService.findSignUpRequest()).thenReturn(List.of(form));
                mockMvc.perform(get("/borrowers/signup-requests"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("로그인하지 않고 전체 회원가입 신청서 조회(실패-국원 이상만 접근가능)")
        @WithAnonymousUser
        void findAllSignUpRequestFailForNotLoginedTest() throws Exception {
                SignUpForm form = new SignUpForm("null", "null", "null", "null", "null", "null", "null", " ");
                when(signUpRequestService.findSignUpRequest()).thenReturn(List.of(form));
                mockMvc.perform(get("/borrowers/signup-requests"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("회원가입 신청(성공-누구나 접근가능하다)")
        @WithAnonymousUser
        void signUpBorrowerSuccessTest() throws Exception {
                SignUpFormDto signUpFormDto = new SignUpFormDto("testId",
                                "examPw12!",
                                "exam@naver.com",
                                "내이름",
                                "010-0000-0000",
                                "10101010101010");
                MockMultipartFile mockStudentIdentificationPhoto = new MockMultipartFile(
                                "student-identification",
                                "hello.png",
                                MediaType.IMAGE_PNG_VALUE,
                                "Hello, World!".getBytes());
                MockMultipartFile mockStudentCouncilFee = new MockMultipartFile(
                                "student-council-fee",
                                "file.png",
                                MediaType.IMAGE_PNG_VALUE,
                                "Hello, World!".getBytes());

                MockMultipartFile signUpFormPart = new MockMultipartFile(
                                "signUpFormDto", // part 이름은 컨트롤러 파라미터 이름과 맞춰야 함
                                "",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsBytes(signUpFormDto));
                SignUpForm signUpForm = signUpFormDto.getSignUpForm("path", "path");
                when(s3Service.uploadFile(mockStudentIdentificationPhoto, "path", "path")).thenReturn("path");
                when(s3Service.uploadFile(mockStudentCouncilFee, "path", "path")).thenReturn("path");
                when(signUpRequestService.saveSignUpRequest(signUpFormDto, mockStudentCouncilFee,
                                mockStudentIdentificationPhoto)).thenReturn(signUpForm);
                mockMvc.perform(multipart("/borrowers/signup-requests")
                                .file(mockStudentIdentificationPhoto)
                                .file(mockStudentCouncilFee)
                                .file(signUpFormPart))
                                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("회원가입 승인(성공-국원이상만 승인가능)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void evaluateRequestSuccessTest() throws Exception {
                EvaluationRequestDto dto = new EvaluationRequestDto(SignUpRequestState.PERMIT, "허락함");
                doNothing().when(signUpRequestService).updateStateAndCreateBorrower(dto, "ddd");

                mockMvc.perform(
                                patch("/borrowers/signup-requests/ddd")
                                                .content(objectMapper.writeValueAsString(dto))
                                                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("대여자 권한으로 회원가입 승인(실패-국원 이상만 승인가능)")
        @WithMockUser(authorities = "BORROWER")
        void evaluateRequestFailForAuthorityTest() throws Exception {
                EvaluationRequestDto dto = new EvaluationRequestDto(SignUpRequestState.PERMIT, null);
                doNothing().when(signUpRequestService).updateStateAndCreateBorrower(dto, "ddd");

                mockMvc.perform(
                                patch("/borrowers/signup-requests/ddd")
                                                .content(objectMapper.writeValueAsString(dto))
                                                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("로그인하지 않고 회원가입 승인(실패-국원 이상만 승인가능)")
        @WithAnonymousUser
        void evaluateRequestFailForAnonymousUserTest() throws Exception {
                EvaluationRequestDto dto = new EvaluationRequestDto(SignUpRequestState.PERMIT, null);
                doNothing().when(signUpRequestService).updateStateAndCreateBorrower(dto, "ddd");

                mockMvc.perform(
                                patch("/borrowers/signup-requests/ddd")
                                                .content(objectMapper.writeValueAsString(dto))
                                                .contentType(ContentType.APPLICATION_JSON.getMimeType()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("회원가입 수정(성공-대여자권한만 수정가능)")
        @WithMockUser(authorities = "BORROWER")
        void rewriteRequestSuccessTest() throws Exception {
                // 서비스로 코드 다 내린 뒤 테스트
                mockMvc.perform(
                                put("/borrowers/signup-requests/ddd")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("국원이상의 권한으로 회원가입 수정(실패-대여자 권한만 수정가능)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void rewriteRequestFailForAuthorityTest() throws Exception {
                mockMvc.perform(
                                put("/borrowers/signup-requests/ddd"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("로그인하지 않고 회원가입 수정(실패-대여자 권한만 수정 가능)")
        @WithAnonymousUser
        void rewirteRequestFailForAnonymousUserTest() throws Exception {
                mockMvc.perform(
                                put("/borrowers/signup-requests/ddd"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("회원가입 신청 취소 수정(성공-대여자권한만 수정가능)")
        @WithMockUser(authorities = "BORROWER")
        void deleteRequestSuccessTest() throws Exception {
                mockMvc.perform(
                                delete("/borrowers/signup-requests/ddd")
                                                .content("password"))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("국원이상의 권한으로 회원가입 신청 취소(실패-대여자 권한만 수정가능)")
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void deleteRequestFailForAuthorityTest() throws Exception {
                mockMvc.perform(
                                delete("/borrowers/signup-requests/ddd")
                                                .content("password"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("로그인하지 않고 회원가입 신청 취소(실패-대여자 권한만 수정 가능)")
        @WithAnonymousUser
        void deleteRequestFailForAnonymousUserTest() throws Exception {
                mockMvc.perform(
                                delete("/borrowers/signup-requests/ddd")
                                                .content("password"))
                                .andExpect(status().isUnauthorized());
        }
}