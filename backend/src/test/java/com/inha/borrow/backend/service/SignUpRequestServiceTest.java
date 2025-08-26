package com.inha.borrow.backend.service;

import com.inha.borrow.backend.cache.IdCache;
import com.inha.borrow.backend.cache.SignUpSessionCache;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.SignUpFormDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.SignUpRequestRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
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
        private JwtTokenService jwtTokenService;
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
                                .email("123")
                                .name("123")
                                .phoneNumber("123")
                                .accountNumber("123")
                                .build();
                signUpForm = SignUpForm.builder()
                                .id("123")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phoneNumber("123")
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
                signUpRequestService.saveSignUpRequest(signUpFormDto, idCard, councilFee);
                SignUpForm result = signUpRequestRepository.findById(signUpFormDto.getId());
                assertThat(result.getAccountNumber()).isEqualTo("123");
                assertThat(result.getIdentityPhoto()).isEqualTo("student-id-card/123idCard.jpg");
                assertThat(result.getStudentCouncilFeePhoto()).isEqualTo("student-council-fee/123councilFee");
        }

        @Test
        @DisplayName("db 오류로 인한 저장 실패")
        void failedSaveSignUpRequestDbError() {
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

                assertThatThrownBy(() -> signUpRequestService.saveSignUpRequest(signUpFormDto, idCard, councilFee))
                                .isInstanceOf(DataAccessException.class);

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

                assertThatThrownBy(() -> signUpRequestService.saveSignUpRequest(signUpFormDto, idCard, councilFee))
                                .isInstanceOf(InvalidValueException.class)
                                .hasMessageContaining("회원가입에 필요한 절차를 모두 수행해주세요.");

        }

        @Test
        @DisplayName("회원가입 목록 불러오기 성공")
        void findSignUpRequest() {
                signUpRequestRepository.save(signUpForm);
                List<SignUpForm> result = signUpRequestService.findSignUpRequest();
                assertThat(result.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("id로 회원가입 찾기 성공")
        void findById() {
                signUpRequestRepository.save(signUpForm);
                SignUpForm result = signUpRequestService.findById("123");
                assertThat(result.getName()).isEqualTo("123");
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
                                .phoneNumber("123")
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
                                .phoneNumber("123")
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
                                .phoneNumber("123")
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
                assertThatThrownBy(() -> signUpRequestService.patchSignUpRequest(signUpForm, idCard, councilFee, "123",
                                "123"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("기존 가입 요청을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("회원가입수정 실패 파일이 비어있음")
        void failedPatchSignUpRequiredMissingFile() {
                signUpRequestRepository.save(signUpForm);
                idCache.setNewUser(signUpForm.getId());
                given(s3Service.uploadFile(any(), anyString(), anyString()))
                                .willReturn("student-id-card/123idCard.jpg", "student-council-fee/123councilFee");
                signUpForm = SignUpForm.builder()
                                .id("321")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phoneNumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                MultipartFile idCard = new MockMultipartFile(
                                "idCard",
                                "idCard.png",
                                "image/png",
                                new byte[0]);
                MultipartFile councilFee = new MockMultipartFile(
                                "councilFee",
                                "councilFee.png",
                                "image/png",
                                new byte[0]);
                assertThatThrownBy(() -> signUpRequestService.patchSignUpRequest(signUpForm, idCard, councilFee, "123",
                                "123"))
                                .isInstanceOf(InvalidValueException.class)
                                .hasMessageContaining("필수 사진이 누락되었습니다.");
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
                                .phoneNumber("123")
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
                assertThatThrownBy(() -> signUpRequestService.patchSignUpRequest(signUpForm, idCard, councilFee, "123",
                                "321"))
                                .isInstanceOf(InvalidValueException.class)
                                .hasMessageContaining("비밀번호가 다릅니다.");
        }

        @Test
        @DisplayName("리퀘스트 삭제 성공")
        void deleteSignUpRequest() {
                signUpForm = SignUpForm.builder()
                                .id("123")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phoneNumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                signUpRequestRepository.save(signUpForm);
                signUpRequestService.deleteSignUpRequest("123", "123");
                assertThatThrownBy(() -> signUpRequestRepository.findById("123"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("요청하신 자원을 찾을수 없습니다.");
        }

        @Test
        @DisplayName("리퀘스트 삭제 실패 패스워드 검증 실패")
        void deleteSignUpRequestPasswordMismatch() {
                signUpForm = SignUpForm.builder()
                                .id("123")
                                .password(passwordEncoder.encode("123"))
                                .email("Test123@test.com")
                                .name("123")
                                .phoneNumber("123")
                                .accountNumber("123")
                                .identityPhoto("student-id-card/123idCard.jpg")
                                .studentCouncilFeePhoto("student-council-fee/123councilFee")
                                .build();
                signUpRequestRepository.save(signUpForm);
                assertThatThrownBy(() -> signUpRequestService.deleteSignUpRequest("123", "321"))
                                .isInstanceOf(InvalidValueException.class)
                                .hasMessageContaining("비밀번호가 다릅니다.");
        }

        @Test
        void print() {
                System.out.println("Injected S3Service: " + s3Service);
                System.out.println("S3Service inside SignUpRequestService: "
                                + ReflectionTestUtils.getField(signUpRequestService, "s3Service"));
        }
}
