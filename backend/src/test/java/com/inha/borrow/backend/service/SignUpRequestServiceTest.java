package com.inha.borrow.backend.service;

import com.inha.borrow.backend.cache.IdCache;
import com.inha.borrow.backend.cache.SignUpSessionCache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.SignUpFormDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.SignUpRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
class SignUpRequestServiceTest {
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private SignUpRequestRepository signUpRequestRepository;
        @Autowired
        private BorrowerRepository borrowerRepository;
        @Autowired
        private SignUpSessionCache signUpSessionCache;
        @MockitoBean
        private S3Service s3Service;
        @Autowired
        private SignUpRequestService signUpRequestService;
        @Autowired
        private IdCache idCache;

        private SignUpFormDto signUpFormDto;
        private SignUpForm signUpForm;
        private EvaluationRequestDto evaluationRequestDto;

        @BeforeEach
        void setUp() {
                signUpFormDto = SignUpFormDto.builder()
                                .id("123")
                                .password("123")
                                .email("123@aaa.com")
                                .name("123")
                                .phonenumber("123")
                                .accountNumber("123")
                                .build();
                signUpForm = SignUpForm.builder()
                                .id("123")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phonenumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                signUpRequestRepository.deleteALL();
                idCache.deleteAll();
        }

        @DisplayName("회원가입 성공")
        @Test
        void saveSignUpRequest() {
                // given
                signUpSessionCache.set(signUpFormDto.getId());
                signUpSessionCache.passwordCheckSuccess(signUpFormDto.getId());
                signUpSessionCache.phoneCheckSuccess(signUpFormDto.getId());
                MultipartFile idCard = new MockMultipartFile(
                                "idCard",
                                "idCard.png",
                                "image/png",
                                "dummy image content".getBytes());
                MultipartFile councilFee = new MockMultipartFile(
                                "councilFee",
                                "councilFee.png",
                                "image/png",
                                "dummy image content".getBytes());

                given(s3Service.uploadFile(any(), anyString(), anyString()))
                                .willReturn("student-id-card/123idCard.jpg", "student-council-fee/123councilFee");
                // when
                // then
                signUpRequestService.saveSignUpRequest(signUpFormDto, idCard, councilFee);
                SignUpForm result = signUpRequestRepository.findById(signUpFormDto.getId());
                assertThat(result.getAccountNumber()).isEqualTo("123");
                assertThat(result.getIdentityPhoto()).isEqualTo("student-id-card/123idCard.jpg");
                assertThat(result.getStudentCouncilFeePhoto()).isEqualTo("student-council-fee/123councilFee");
        }

        @Test
        @DisplayName("회원가입 저장 실패 회원가입 절차 미실행")
        void failedSaveSignUpRequestSignUpPassFailed() {
                signUpSessionCache.set(signUpFormDto.getId());
                signUpSessionCache.passwordCheckSuccess(signUpFormDto.getId());
                MultipartFile idCard = new MockMultipartFile(
                                "idCard",
                                "idCard.png",
                                "image/png",
                                "dummy image content".getBytes());
                MultipartFile councilFee = new MockMultipartFile(
                                "councilFee",
                                "councilFee.png",
                                "image/png",
                                "dummy image content".getBytes());

                InvalidValueException ex = assertThrows(InvalidValueException.class, () -> {
                        signUpRequestService.saveSignUpRequest(signUpFormDto, idCard, councilFee);
                });

                assertThat(ex.getErrorMessage()).isEqualTo(ApiErrorCode.SIGN_UP_PASS_FAILED.getMessage());
                assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.SIGN_UP_PASS_FAILED.name());
        }

        @Test
        @DisplayName("회원가입 목록 불러오기 성공")
        void findSignUpRequest() {
                signUpRequestRepository.save(signUpForm);
                List<SignUpForm> result = signUpRequestService.findSignUpRequest();
                assertThat(result.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("id로 회원가입 찾기 성공(관리자, 대여자 본인)")
        void findByIdSuccess() {
                // given
                String password = "123";
                signUpRequestRepository.save(signUpForm);
                Admin admin = Admin.builder()
                                .id("admin")
                                .authorities(List.of(new SimpleGrantedAuthority("DIVISION_MEMBER")))
                                .build();
                // when
                // 관리자 권한으로 요청한 경우
                SignUpForm admin_result = signUpRequestService.findById(admin, "123", null);
                // 사용자 권한으로 요청한 경우
                SignUpForm requester_result = signUpRequestService.findById(null, "123", password);
                // then
                // 관리자 권한 요청 테스트
                assertThat(admin_result.getId()).isEqualTo("123");
                // 사용자 권한 요청 테스트
                assertThat(requester_result.getId()).isEqualTo("123");
        }

        @Test
        @DisplayName("id로 회원가입 찾기 실패(비밀번호 틀림)")
        void findByIdFailForAuthority() {
                // given
                String password = "1234";
                signUpRequestRepository.save(signUpForm);
                // when
                // then
                // 관리자 권한 요청 테스트
                assertThrows(InvalidValueException.class, () -> {
                        signUpRequestService.findById(null, "123", password);
                });
        }

        @Test
        @DisplayName("state 변경 후 저장 성공")
        void updateStateAndCreateBorrower() {
                borrowerRepository.deleteAll();
                signUpForm = SignUpForm.builder()
                                .id("123")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phonenumber("123")
                                .accountNumber("123")
                                .identityPhoto("123")
                                .studentCouncilFeePhoto("123")
                                .build();
                evaluationRequestDto = EvaluationRequestDto.builder()
                                .state(SignUpRequestState.PERMIT)
                                .rejectReason(".")
                                .build();
                signUpRequestRepository.save(signUpForm);
                signUpRequestService.updateStateAndCreateBorrower(evaluationRequestDto, signUpForm.getId());
                Borrower result = borrowerRepository.findById(signUpForm.getId());
                assertThat(result.getName()).isEqualTo(signUpForm.getName());
        }

        @Test
        @DisplayName("회원가입 수정")
        void patchSignUpRequest() {
                signUpRequestRepository.save(signUpForm);
                idCache.setNewUser(signUpForm.getId());
                given(s3Service.uploadFile(any(), anyString(), anyString()))
                                .willReturn("student-id-card/123idCard.jpg", "student-council-fee/123councilFee");
                signUpForm = SignUpForm.builder()
                                .id("321")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phonenumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                MultipartFile idCard = new MockMultipartFile(
                                "idCard",
                                "idCard.png",
                                "image/png",
                                "dummy image content".getBytes());
                MultipartFile councilFee = new MockMultipartFile(
                                "councilFee",
                                "councilFee.png",
                                "image/png",
                                "dummy image content".getBytes());
                signUpRequestService.patchSignUpRequest(signUpForm, idCard, councilFee, "123", "123");
                SignUpForm result = signUpRequestRepository.findById("321");
                assertThat(result.getId()).isEqualTo("321");
        }

        @Test
        @DisplayName("회원가입수정 실패 리퀘스트 미존재")
        void failedPatchSignUpRequestNotFound() {
                signUpRequestRepository.save(signUpForm);
                idCache.contains("123");
                given(s3Service.uploadFile(any(), anyString(), anyString()))
                                .willReturn("student-id-card/123idCard.jpg", "student-council-fee/123councilFee");
                signUpForm = SignUpForm.builder()
                                .id("321")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phonenumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                MultipartFile idCard = new MockMultipartFile(
                                "idCard",
                                "idCard.png",
                                "image/png",
                                "dummy image content".getBytes());
                MultipartFile councilFee = new MockMultipartFile(
                                "councilFee",
                                "councilFee.png",
                                "image/png",
                                "dummy image content".getBytes());
                ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                                () -> signUpRequestService.patchSignUpRequest(signUpForm, idCard, councilFee, "123",
                                                "123"));
                assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND.name());
                assertThat(ex.getErrorMessage()).isEqualTo(ApiErrorCode.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("회원가입수정 실패 비밀번호 검증 실패")
        void failedPatchSignUpIncorrectPassword() {
                signUpRequestRepository.save(signUpForm);
                idCache.setNewUser("123");
                given(s3Service.uploadFile(any(), anyString(), anyString()))
                                .willReturn("student-id-card/123idCard.jpg", "student-council-fee/123councilFee");
                signUpForm = SignUpForm.builder()
                                .id("321")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phonenumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                MultipartFile idCard = new MockMultipartFile(
                                "idCard",
                                "idCard.png",
                                "image/png",
                                "dummy image content".getBytes());
                MultipartFile councilFee = new MockMultipartFile(
                                "councilFee",
                                "councilFee.png",
                                "image/png",
                                "dummy image content".getBytes());
                InvalidValueException ex = assertThrows(InvalidValueException.class,
                                () -> signUpRequestService.patchSignUpRequest(signUpForm, idCard, councilFee, "123",
                                                "321"));
                assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.INCORRECT_PASSWORD.name());
                assertThat(ex.getErrorMessage()).isEqualTo(ApiErrorCode.INCORRECT_PASSWORD.getMessage());
        }

        @Test
        @DisplayName("리퀘스트 삭제 성공")
        void deleteSignUpRequest() {
                signUpForm = SignUpForm.builder()
                                .id("123")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phonenumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                signUpRequestRepository.save(signUpForm);
                signUpRequestService.deleteSignUpRequest("123", "123");
                ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
                        signUpRequestRepository.findById("123");
                });
                assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.NOT_FOUND.name());
                assertThat(ex.getErrorMessage()).isEqualTo(ApiErrorCode.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("리퀘스트 삭제 실패 패스워드 검증 실패")
        void deleteSignUpRequestPasswordMismatch() {
                signUpForm = SignUpForm.builder()
                                .id("123")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phonenumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                signUpRequestRepository.save(signUpForm);
                InvalidValueException ex = assertThrows(InvalidValueException.class, () -> {
                        signUpRequestService.deleteSignUpRequest("123", "321");
                });
                assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.INCORRECT_PASSWORD.name());
                assertThat(ex.getErrorMessage()).isEqualTo(ApiErrorCode.INCORRECT_PASSWORD.getMessage());
        }

        @Test
        void print() {
                System.out.println("Injected S3Service: " + s3Service);
                System.out.println("S3Service inside SignUpRequestService: "
                                + ReflectionTestUtils.getField(signUpRequestService, "s3Service"));
        }
}
