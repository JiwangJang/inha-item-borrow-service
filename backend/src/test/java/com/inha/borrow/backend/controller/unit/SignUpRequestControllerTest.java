package com.inha.borrow.backend.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.cache.IdCache;
import com.inha.borrow.backend.config.AuthConfig;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.controller.SignUpRequestController;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.handler.GlobalErrorHandler;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.SignUpFormDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.service.S3Service;
import com.inha.borrow.backend.service.SignUpRequestService;

import net.nurigo.sdk.message.response.ErrorResponse;

@WebMvcTest(controllers = SignUpRequestController.class)
@Import({ GlobalErrorHandler.class, AuthConfig.class })
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

        private ResultActions performSignUpPost(SignUpFormDto dto) throws Exception {
                MockMultipartFile studentId = new MockMultipartFile(
                                "student-identification", "id.png", MediaType.IMAGE_PNG_VALUE, "x".getBytes());
                MockMultipartFile council = new MockMultipartFile(
                                "student-council-fee", "fee.png", MediaType.IMAGE_PNG_VALUE, "x".getBytes());
                MockMultipartFile body = new MockMultipartFile(
                                "signUpFormDto", "", MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsBytes(dto));

                // 서비스 호출이 일어나지 않는(검증 실패) 케이스들에서는 굳이 stubbing 불필요
                return mockMvc.perform(multipart("/borrowers/signup-requests")
                                .file(studentId).file(council).file(body));
        }

        @Test
        @DisplayName("회원가입 신청서 단건 조회(성공-단건 조회 경로는 누구나 접근가능하다)")
        @WithAnonymousUser
        void findBySignUpRequestId() throws Exception {
                mockMvc.perform(
                                get("/borrowers/signup-requests/ddd"))
                                .andExpect(status().isOk());
        }

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

        @ParameterizedTest
        @DisplayName("회원가입 신청(실패-허용되지 않는 값)")
        @CsvSource({
                        // id, password, email, name, phonenumber, accountNumber, expected, message
                        "testId, Abcdef1!2, test@a.com, 내이름, 010-0000-0000, 10101010101010, 201, ' '",
                        "t, Abcdef1!2, test@a.com, 내이름, 010-0000-0000, 10101010101010, 400, 아이디는 영어대소문자와 숫자로 4~10자여야 합니다.",
                        "testId, abc, test@a.com, 내이름, 010-0000-0000, 10101010101010, 400, '비밀번호는 영어 대소문자와 숫자, 특수기호((!@#$%^&*()_\\-+=)를 포함해 9~13자여야 합니다.'",
                        "testId, Abcdef1!2, nope, 내이름, 010-0000-0000, 10101010101010, 400, 이메일 형식에 맞지 않습니다.",
                        "testId, Abcdef1!2, test@a.com, ' ', 010-0000-0000, 10101010101010, 400, 이름을 작성해주세요.",
                        "testId, Abcdef1!2, test@a.com, 내이름, ' ', 10101010101010, 400, 핸드폰 번호를 기입해주세요.",
                        "testId, Abcdef1!2, test@a.com, 내이름, 010-0000-0000, ' ', 400, 환불계좌번호를 기입해주세요."
        })
        @WithAnonymousUser
        void signUpBorrowerFailForInvalidValueTest(String id, String password, String email, String name,
                        String phonenumber, String accountNumber, int expected, String errorMsg) throws Exception {
                SignUpFormDto dto = new SignUpFormDto(id, password, email, name, phonenumber, accountNumber);
                ApiResponse<Object> apiResponse = new ApiResponse<Object>(false,
                                new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(), errorMsg));
                if (expected == 201) {
                        when(s3Service.uploadFile(any(), anyString(), anyString())).thenReturn("path");
                        when(signUpRequestService.saveSignUpRequest(any(), any(), any()))
                                        .thenReturn(dto.getSignUpForm("path", "path"));
                        performSignUpPost(dto)
                                        .andExpect(status().is(expected));
                        return;
                }
                performSignUpPost(dto)
                                .andExpect(status().is(expected))
                                .andExpect(content().json(objectMapper.writeValueAsString(apiResponse)));
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

        @ParameterizedTest
        @DisplayName("회원가입 승인 실패(허락되지 않는 값)")
        @CsvSource({
                        // SignUpRequestState, rejectReason, expected, message
                        "PERMIT, 허락함, 200, ' '",
                        "REJECT, 안됨, 200, ' '",
                        "PERMIT, ' ', 400, 회원가입 거절 이유는 필수입니다.",
                        "' ', 이유, 400, 회원가입 요청 상태는 필수입니다."
        })
        @WithMockUser(authorities = "DIVISION_MEMBER")
        void evaluateRequestFailForArgument(String signUpRequestState, String rejectReason, int expected,
                        String errorMsg) throws Exception {
                SignUpRequestState state = null;
                if (!signUpRequestState.equals(" ")) {
                        state = SignUpRequestState.valueOf(signUpRequestState);
                }
                EvaluationRequestDto dto = new EvaluationRequestDto(state,
                                rejectReason);
                if (expected == 200) {
                        mockMvc.perform(patch("/borrowers/signup-requests/exam-signup-requets-id")
                                        .content(objectMapper.writeValueAsString(dto))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andExpect(status().isOk());
                        return;
                }
                ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(), errorMsg);
                ApiResponse<Object> apiResponse = new ApiResponse<Object>(false, errorResponse);
                mockMvc.perform(patch("/borrowers/signup-requests/exam-signup-requets-id")
                                .content(objectMapper.writeValueAsString(dto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().is(expected))
                                .andExpect(content().json(objectMapper.writeValueAsString(apiResponse)));
        }

        @Test
        @DisplayName("로그인하지 않고 회원가입 수정(성공-수정 경로는 누구나 접근 가능하다)")
        @WithAnonymousUser
        void rewirteRequestFailForAnonymousUserTest() throws Exception {
                // 이건 수정 메서드 수정된거 보고 수정하기
                mockMvc.perform(
                                put("/borrowers/signup-requests/ddd"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("로그인하지 않고 회원가입 신청 취소(성공-취소 경로는 누구나 접근 가능하다)")
        @WithAnonymousUser
        void deleteRequestFailForAnonymousUserTest() throws Exception {
                doNothing().when(signUpRequestService).deleteSignUpRequest(anyString(), anyString());
                mockMvc.perform(
                                delete("/borrowers/signup-requests/ddd")
                                                .content("password"))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("회원가입 신청 취소(실패-비밀번호 다름)")
        @WithAnonymousUser
        void deleteRequestFailForInvalidPasswordTest() throws Exception {
                doThrow(InvalidValueException.class).when(signUpRequestService).deleteSignUpRequest(anyString(),
                                anyString());
                mockMvc.perform(delete("/borrowers/signup-requests/ddd")
                                .content("password"))
                                .andExpect(status().isBadRequest());
        }
}