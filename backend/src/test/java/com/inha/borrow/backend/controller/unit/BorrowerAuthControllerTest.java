package com.inha.borrow.backend.controller.unit;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.controller.BorrowerAuthController;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.auth.PasswordVerifyRequestDto;
import com.inha.borrow.backend.model.dto.auth.SMSCodeRequestDto;
import com.inha.borrow.backend.model.dto.auth.SMSCodeVerifyDto;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.dto.response.ErrorResponse;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.service.BorrowerVerificationService;

// 해당 컨트롤러는 누구나 접근가능하므로 WithMockUser사용
@WithMockUser
@WebMvcTest(controllers = BorrowerAuthController.class)
public class BorrowerAuthControllerTest {
    @MockitoBean
    private BorrowerVerificationService borrowerVerificationService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("아이디가 조건에 맞게 보내진 경우(성공)")
    void verifyIdSuccessTest() throws Exception {
        // given
        String testId = "TestId1";
        doNothing().when(borrowerVerificationService).verifyId(testId);
        // when
        // then
        mockMvc.perform(get("/borrowers/auth/id-check").param("id", testId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("아이디가 조건에 맞지 않게 보내진 경우(실패)")
    void verifyIdFailForValidationTest() throws Exception {
        // given
        String testId = "s";
        ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(),
                "아이디는 영어대소문자와 숫자를 조합해 4~10자여야 합니다.");
        ApiResponse<ErrorResponse> apiResponse = new ApiResponse<>(false, errorResponse);
        // when
        // then
        mockMvc.perform(get("/borrowers/auth/id-check?id=" + testId))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    @DisplayName("아이디가 이미 등록된 경우(실패)")
    void verifyIdFailForExistIdTest() throws Exception {
        // given
        String testId = "TestId1";
        doThrow(new InvalidValueException(ApiErrorCode.EXIST_ID.name(), ApiErrorCode.EXIST_ID.getMessage()))
                .when(borrowerVerificationService).verifyId(testId);
        ErrorResponse errorResponse = new ErrorResponse(ApiErrorCode.EXIST_ID.name(),
                ApiErrorCode.EXIST_ID.getMessage());
        ApiResponse<ErrorResponse> apiResponse = new ApiResponse<>(false, errorResponse);
        // when
        // then
        mockMvc.perform(get("/borrowers/auth/id-check").param("id", testId))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    @DisplayName("비밀번호가 올바르게 작성된 경우(성공)")
    void verifyPasswordSuccessTest() throws Exception {
        // given
        String testId = "testId";
        String testPassword = "testPass1!";
        PasswordVerifyRequestDto dto = new PasswordVerifyRequestDto();
        dto.setId(testId);
        dto.setPassword(testPassword);
        doNothing().when(borrowerVerificationService).verifyPassword(testId, testPassword);
        // when
        // then
        mockMvc.perform(patch("/borrowers/auth/password-check")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 유효성검사 실패")
    void verifyPasswordFailForInValidPasswordTest() throws Exception {
        // given
        String testId = "testId";
        String testPassword = "testPass1";
        PasswordVerifyRequestDto dto = new PasswordVerifyRequestDto();
        dto.setId(testId);
        dto.setPassword(testPassword);

        ApiResponse<ErrorResponse> expect = new ApiResponse<ErrorResponse>(false,
                new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(),
                        "비밀번호는 영어 대소문자와 숫자, 특수기호((!@#$%^&*()_\\-+=)를 포함해 9~13자여야 합니다."));
        // when
        // then
        mockMvc.perform(patch("/borrowers/auth/password-check")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(expect)));
    }

    @Test
    @DisplayName("비밀번호를 입력안해서 실패")
    void verifyPasswordFailForBlankPasswordTest() throws Exception {
        // given
        String testId = "testId";
        String testPassword = "";
        PasswordVerifyRequestDto dto = new PasswordVerifyRequestDto();
        dto.setId(testId);
        dto.setPassword(testPassword);

        ApiResponse<ErrorResponse> expect = new ApiResponse<ErrorResponse>(false,
                new ErrorResponse(ApiErrorCode.INVALID_VALUE.name(),
                        "비밀번호는 영어 대소문자와 숫자, 특수기호((!@#$%^&*()_\\-+=)를 포함해 9~13자여야 합니다."));
        // when
        // then
        mockMvc.perform(patch("/borrowers/auth/password-check")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(expect)));
    }

    @Test
    @DisplayName("없는 회원가입 세션에 대해 비밀번호 검증시도 실패")
    void verifyPasswordFailForNonExsitUserTest() throws Exception {
        // given
        String testId = "testId";
        String testPassword = "testPass1!";
        PasswordVerifyRequestDto dto = new PasswordVerifyRequestDto();
        dto.setId(testId);
        dto.setPassword(testPassword);
        doThrow(new ResourceNotFoundException(ApiErrorCode.NOT_FOUND.name(), ApiErrorCode.NOT_FOUND.getMessage()))
                .when(borrowerVerificationService)
                .verifyPassword(testId, testPassword);

        ApiResponse<ErrorResponse> expect = new ApiResponse<ErrorResponse>(false,
                new ErrorResponse(ApiErrorCode.NOT_FOUND.name(),
                        ApiErrorCode.NOT_FOUND.getMessage()));
        // when
        // then
        mockMvc.perform(patch("/borrowers/auth/password-check")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(expect)));
    }

    @Test
    @DisplayName("인증번호 발송(성공)")
    void sendSMSCodeSuccessTest() throws Exception {
        // given
        SMSCodeRequestDto dto = new SMSCodeRequestDto("testId", "0000000000");
        doNothing().when(borrowerVerificationService).sendSMSCode(dto.getId(), dto.getPhoneNumber());
        // when
        // then
        mockMvc.perform(post("/borrowers/auth/send-sms-code")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("없는 회원가입 세션에 대한 인증번호 발송 요청(실패)")
    void sendSMSCodeFailTest() throws Exception {
        // given
        SMSCodeRequestDto dto = new SMSCodeRequestDto("testId", "0000000000");
        doThrow(new ResourceNotFoundException(ApiErrorCode.NOT_FOUND.name(), ApiErrorCode.NOT_FOUND.getMessage()))
                .when(borrowerVerificationService)
                .sendSMSCode(dto.getId(), dto.getPhoneNumber());
        ApiResponse<ErrorResponse> expect = new ApiResponse<ErrorResponse>(false,
                new ErrorResponse(ApiErrorCode.NOT_FOUND.name(),
                        ApiErrorCode.NOT_FOUND.getMessage()));
        // when
        // then
        mockMvc.perform(post("/borrowers/auth/send-sms-code")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(expect)));
    }

    @Test
    @DisplayName("인증번호 검증(성공)")
    void verifySMSCodeSuccessTest() throws Exception {
        // given
        SMSCodeVerifyDto dto = new SMSCodeVerifyDto("test", "123456");
        doNothing().when(borrowerVerificationService).verifySMSCode(dto.getId(), dto.getCode());
        // when
        // then
        mockMvc.perform(patch("/borrowers/auth/verify-sms-code")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("다른 인증코드 발송 (실패)")
    void verifySMSCodeFailForIncorrectCodeTest() throws Exception {
        // given
        SMSCodeVerifyDto dto = new SMSCodeVerifyDto("test", "123456");
        doThrow(new InvalidValueException(ApiErrorCode.INCORRECT_CODE.name(), ApiErrorCode.INCORRECT_CODE.getMessage()))
                .when(borrowerVerificationService)
                .verifySMSCode(dto.getId(), dto.getCode());
        ApiResponse<ErrorResponse> expect = new ApiResponse<ErrorResponse>(false,
                new ErrorResponse(ApiErrorCode.INCORRECT_CODE.name(), ApiErrorCode.INCORRECT_CODE.getMessage()));
        // when
        // then
        mockMvc.perform(patch("/borrowers/auth/verify-sms-code")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(expect)));
    }

    @Test
    @DisplayName("없는 회원가입 세션에 대한 코드 발송 (실패)")
    void verifySMSCodeFailForNonExistUserTest() throws Exception {
        // given
        SMSCodeVerifyDto dto = new SMSCodeVerifyDto("test", "123456");
        doThrow(new ResourceNotFoundException(ApiErrorCode.NOT_FOUND.name(), ApiErrorCode.NOT_FOUND.getMessage()))
                .when(borrowerVerificationService)
                .verifySMSCode(dto.getId(), dto.getCode());
        ApiResponse<ErrorResponse> expect = new ApiResponse<ErrorResponse>(false,
                new ErrorResponse(ApiErrorCode.NOT_FOUND.name(), ApiErrorCode.NOT_FOUND.getMessage()));
        // when
        // then
        mockMvc.perform(patch("/borrowers/auth/verify-sms-code")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(ContentType.APPLICATION_JSON.getMimeType())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(expect)));
    }
}
