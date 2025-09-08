package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import(SignUpRequestRepository.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class SignUpRequestRepositoryTest {

    @Autowired
    private SignUpRequestRepository signUpRequestRepository;

    private SignUpForm signUpForm;

    @BeforeEach
    void setUp() {
        signUpRequestRepository.deleteALL();
        signUpForm = SignUpForm.builder()
                .id("123")
                .password("123")
                .name("123")
                .email("123")
                .phonenumber("123")
                .identityPhoto("123")
                .studentCouncilFeePhoto("123")
                .accountNumber("123")
                .build();
        signUpRequestRepository.save(signUpForm);
    }

    @Test
    @DisplayName("저장 성공")
    void save() {
        signUpRequestRepository.deleteALL();
        SignUpForm result = signUpRequestRepository.save(signUpForm);
        assertThat(result.getEmail()).isEqualTo("123");
        assertThat(result.getPhonenumber()).isEqualTo("123");

    }

    @Test
    @DisplayName("전체 조회 성공")
    void findAll() {
        List<SignUpForm> result = signUpRequestRepository.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("단일 조회 성공")
    void findById() {
        SignUpForm result = signUpRequestRepository.findById("123");
        assertThat(result.getName()).isEqualTo("123");
    }

    @Test
    @DisplayName("단일 조회 (실패 ID 미존재)")
    void FindByIdFailNotFoundId() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> signUpRequestRepository.findById("456"));
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND.name());
        assertThat(ex.getErrorMessage()).isEqualTo("회원가입 신청내역이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호로 단일 조회 성공")
    void findPasswordById() {
        String result = signUpRequestRepository.findPasswordById("123");
        assertThat(result).isEqualTo("123");
    }

    @Test
    @DisplayName("비밀번호로 단일 조회 (실패 ID 미존재)")
    void PasswordByIdFailNotFoundId() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> signUpRequestRepository.findPasswordById("456"));
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND.name());
        assertThat(ex.getErrorMessage()).isEqualTo("회원가입 신청내역이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("평가 저장 성공")
    void patchEvaluation() {
        signUpRequestRepository.deleteALL();
        signUpRequestRepository.save(signUpForm);
        EvaluationRequestDto evaluation = new EvaluationRequestDto();
        evaluation.setRejectReason("123");
        evaluation.setState(SignUpRequestState.PERMIT);
        signUpRequestRepository.patchEvaluation(evaluation, "123");
        EvaluationRequestDto result = signUpRequestRepository.findStateAndReject("123");
        assertThat(result.getState()).isEqualTo(SignUpRequestState.PERMIT);
        assertThat(result.getRejectReason()).isEqualTo("123");
    }

    @Test
    @DisplayName("리퀘스트 수정 성공")
    void patchSignUpRequest() {
        signUpRequestRepository.deleteALL();
        signUpRequestRepository.save(signUpForm);
        signUpForm.setId("456");
        signUpForm.setPassword("456");
        signUpForm.setAccountNumber("456");
        signUpRequestRepository.patchSignUpRequest(signUpForm, "123");
        SignUpForm result = signUpRequestRepository.findById("456");
        assertThat(result.getId()).isEqualTo("456");
        assertThat(result.getPassword()).isEqualTo("456");
        assertThat(result.getAccountNumber()).isEqualTo("456");
    }

    @Test
    @DisplayName("리퀘스트 수정 (실패 ID 미존재")
    void PatchSignUpRequestFailNotFoundId() {
        signUpRequestRepository.deleteALL();
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> signUpRequestRepository.patchSignUpRequest(signUpForm, "123"));
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND.name());
        assertThat(ex.getErrorMessage()).isEqualTo("회원가입 신청내역이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("리퀘스트 삭제 성공")
    void deleteSignUpRequest() {
        signUpRequestRepository.deleteSignUpRequest("123", "123");
        assertThat(signUpRequestRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("리퀘스트 삭제 (실패 ID 미존재")
    void deleteSignUpRequestFailNotFoundId() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> signUpRequestRepository.deleteSignUpRequest("321", "123"));
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND.name());
        assertThat(ex.getErrorMessage()).isEqualTo("회원가입 신청내역이 존재하지 않습니다.");
    }
}
