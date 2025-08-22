package com.inha.borrow.backend.repository;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class SignUpRequestRepositoryTest {

    @Autowired
    private SignUpRequestRepository signUpRequestRepository;

    private SignUpForm signUpForm;

    @BeforeEach
    void setUp(){
        signUpRequestRepository.deleteALL();
        signUpForm = SignUpForm.builder()
                .id("123")
                .password("123")
                .name("123")
                .email("123")
                .phoneNumber("123")
                .identityPhoto("123")
                .studentCouncilFeePhoto("123")
                .accountNumber("123")
                .build();
        signUpRequestRepository.save(signUpForm);
    }

    @Test
    void save() {
        signUpRequestRepository.deleteALL();
        SignUpForm result = signUpRequestRepository.save(signUpForm);
        assertThat(result.getEmail()).isEqualTo("123");
        assertThat(result.getPhoneNumber()).isEqualTo("123");

    }

    @Test
    void findAll() {
        List<SignUpForm> result = signUpRequestRepository.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void findById() {
        SignUpForm result = signUpRequestRepository.findById("123");
        assertThat(result.getName()).isEqualTo("123");
    }
    @Test
    void failedFindById(){
        assertThatThrownBy(() -> signUpRequestRepository.findById("456"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void findPasswordById() {
        String result = signUpRequestRepository.findPasswordById("123");
        assertThat(result).isEqualTo("123");
    }
    @Test
    void failedPasswordById(){
        assertThatThrownBy(() -> signUpRequestRepository.findPasswordById("456"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND.getMessage());
    }


    @Test
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
    void patchSignUpRequest() {
        signUpRequestRepository.deleteALL();
        signUpRequestRepository.save(signUpForm);
        signUpForm.setId("456");
        signUpForm.setPassword("456");
        signUpForm.setAccountNumber("456");
        signUpRequestRepository.patchSignUpRequest(signUpForm,"123");
        SignUpForm result = signUpRequestRepository.findById("456");
        assertThat(result.getId()).isEqualTo("456");
        assertThat(result.getPassword()).isEqualTo("456");
        assertThat(result.getAccountNumber()).isEqualTo("456");
    }

    @Test
    void failedPatchSignUpRequest(){
        signUpRequestRepository.deleteALL();
        assertThatThrownBy(() -> signUpRequestRepository.patchSignUpRequest(signUpForm,"123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ApiErrorCode.NOT_FOUND.getMessage());
    }


    @Test
    void deleteSignUpRequest() {
        signUpRequestRepository.deleteSignUpRequest("123","123");
        assertThat(signUpRequestRepository.findAll()).isEmpty();
    }
}
