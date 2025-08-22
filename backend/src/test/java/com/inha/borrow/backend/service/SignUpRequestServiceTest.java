package com.inha.borrow.backend.service;

import com.inha.borrow.backend.cache.SignUpSessionCache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.SignUpFormDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.SignUpRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpRequestServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SignUpRequestRepository signUpRequestRepository;
    @Mock
    private BorrowerRepository borrowerRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private SignUpSessionCache signUpSessionCache;
    @Mock
    private S3Service s3Service;
    @InjectMocks
    private SignUpRequestService signUpRequestService;

    private SignUpFormDto signUpFormDto;

    @BeforeEach
    void setUp(){
        signUpFormDto = SignUpFormDto.builder()
                .id("123")
                .password("123")
                .email("123")
                .name("123")
                .phoneNumber("123")
                .accountNumber("123")
                .build();
    }


    @DisplayName("회원가입 성공")
    @Test
    void saveSignUpRequest() {
        MultipartFile idCard = mock(MultipartFile.class);
        MultipartFile councilFee = mock(MultipartFile.class);

        given(signUpSessionCache.isAllPassed("123")).willReturn(true);
        given(passwordEncoder.encode("123")).willReturn("encodedPassword");
        given(s3Service.uploadFile(any(), any(), any()))
                .willReturn("idCard.jpg", "councilFee.jpg");

        SignUpForm signUpForm = SignUpForm.builder()
                .id("123")
                .password("123")
                .email("123")
                .name("123")
                .phoneNumber("123")
                .identityPhoto("idCard.jpg")
                .studentCouncilFeePhoto("councilFee.jpg")
                .accountNumber("123")
                .build();
        given(signUpRequestRepository.save(any())).willReturn(signUpForm);

        SignUpForm result = signUpRequestService.saveSignUpRequest(signUpFormDto,idCard,councilFee);

        assertThat(result.getId()).isEqualTo("123");
        assertThat(result.getIdentityPhoto()).isEqualTo("idCard.jpg");
        assertThat(result.getStudentCouncilFeePhoto()).isEqualTo("councilFee.jpg");
    }
    @Test
    @DisplayName("캐쉬검증 실패로 인한 저장 실패")
    void failedSaveSignUpRequestCache() {
        given(signUpSessionCache.isAllPassed("123")).willReturn(false);

        assertThatThrownBy(() -> signUpRequestService.saveSignUpRequest(signUpFormDto, null, null))
                .isInstanceOf(InvalidValueException.class);
    }
    @Test
    @DisplayName("db 오류로 인한 저장 실패")
    void failedSaveSignUpRequestDbError() {
        MultipartFile idCard = mock(MultipartFile.class);
        MultipartFile councilFee = mock(MultipartFile.class);

        given(signUpSessionCache.isAllPassed("123")).willReturn(true);
        given(passwordEncoder.encode("123")).willReturn("encoded-password");
        given(s3Service.uploadFile(any(),any(),any())).willReturn("idCard.jpg", "councilFee.jpg");
        given(signUpRequestRepository.save(any())).willThrow(new DataAccessException("DB Error") {
        });

        assertThatThrownBy(() -> signUpRequestService.saveSignUpRequest(signUpFormDto, idCard, councilFee))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("회원가입 목록 불러오기")
    void findSignUpRequest() {
        MultipartFile idCard = mock(MultipartFile.class);
        MultipartFile councilFee = mock(MultipartFile.class);

        given(signUpSessionCache.isAllPassed("123")).willReturn(true);
        given(passwordEncoder.encode("123")).willReturn("encodedPassword");
        given(s3Service.uploadFile(any(), any(), any()))
                .willReturn("idCard.jpg", "councilFee.jpg");

        SignUpForm signUpForm = SignUpForm.builder()
                .id("123")
                .password("encodedPassword")
                .email("123")
                .name("123")
                .phoneNumber("123")
                .identityPhoto("idCard.jpg")
                .studentCouncilFeePhoto("councilFee.jpg")
                .accountNumber("123")
                .build();

        given(signUpRequestRepository.save(any())).willReturn(signUpForm);
        given(signUpRequestRepository.findAll()).willReturn(List.of(signUpForm));

        signUpRequestService.saveSignUpRequest(signUpFormDto, idCard, councilFee);
        List<SignUpForm> signUpForms = signUpRequestService.findSignUpRequest();

        assertThat(signUpForms.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("id로 회원가입 찾기")
    void findById() {
        MultipartFile idCard = mock(MultipartFile.class);
        MultipartFile councilFee = mock(MultipartFile.class);

        given(signUpSessionCache.isAllPassed("123")).willReturn(true);
        given(passwordEncoder.encode("123")).willReturn("encodedPassword");
        given(s3Service.uploadFile(any(), any(), any()))
                .willReturn("idCard.jpg", "councilFee.jpg");

        SignUpForm signUpForm = SignUpForm.builder()
                .id("123")
                .password("encodedPassword")
                .email("123")
                .name("123")
                .phoneNumber("123")
                .identityPhoto("idCard.jpg")
                .studentCouncilFeePhoto("councilFee.jpg")
                .accountNumber("123")
                .build();

        given(signUpRequestRepository.save(any())).willReturn(signUpForm);
        given(signUpRequestRepository.findById(any())).willReturn(signUpForm);

        signUpRequestService.saveSignUpRequest(signUpFormDto, idCard, councilFee);
        SignUpForm signUpForm1 = signUpRequestService.findById("123");

        assertThat(signUpForm1.getName()).isEqualTo("123");
    }

    @Test
    @DisplayName("state 변경 후 저장")
    void updateStateAndCreateBorrower() {
        // given
        EvaluationRequestDto evaluationRequestDto = new EvaluationRequestDto(SignUpRequestState.PERMIT, null);
        SignUpForm signUpForm = SignUpForm.builder().id("123").build();
        BorrowerDto mockBorrowerDto = new BorrowerDto();

        SignUpRequestService spyService = Mockito.spy(signUpRequestService);

        // private transition() 메서드를 모킹
        doReturn(mockBorrowerDto).when(spyService).transition(any(SignUpForm.class));

        given(signUpRequestRepository.findById("123")).willReturn(signUpForm);
        given(jwtTokenService.createToken("123")).willReturn("mockRefreshToken");

        // when
        spyService.updateStateAndCreateBorrower(evaluationRequestDto, "123");

        // then
        verify(s3Service).deleteAllFile("bucket", "123");
        verify(borrowerRepository).save(mockBorrowerDto);
        verify(signUpRequestRepository).patchEvaluation(evaluationRequestDto, "123");
        assertThat(mockBorrowerDto.getRefreshToken()).isEqualTo("mockRefreshToken");

    }

    @Test
    @DisplayName("회원가입 수정")
    void patchSignUpRequest() {
        SignUpForm signUpForm = SignUpForm.builder()
                .id("123")
                .password("123")
                .email("456")
                .name("테스트")
                .phoneNumber("01012345678")
                .identityPhoto("idCard.jpg")
                .studentCouncilFeePhoto("councilFee.jpg")
                .accountNumber("123")
                .build();
        given(passwordEncoder.matches("123", "123")).willReturn(true);
        given(signUpRequestRepository.findPasswordById("123")).willReturn("123");
        given(passwordEncoder.encode("123")).willReturn("456");

        signUpRequestService.patchSignUpRequest(signUpForm, "123", "123");
        ArgumentCaptor<SignUpForm> captor = ArgumentCaptor.forClass(SignUpForm.class);
        verify(signUpRequestRepository).patchSignUpRequest(captor.capture(),eq("123"));

        SignUpForm saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo("456");
        assertThat(saved.getName()).isEqualTo(signUpForm.getName());
    }
    @Test
    @DisplayName("회원가입수정 실패")
    void failedPatchSignUp(){
        SignUpForm signUpForm = SignUpForm.builder()
                .id("123")
                .password("123")
                .email("456")
                .name("테스트")
                .phoneNumber("01012345678")
                .identityPhoto("idCard.jpg")
                .studentCouncilFeePhoto("councilFee.jpg")
                .accountNumber("123")
                .build();
        assertThatThrownBy(() -> signUpRequestService.patchSignUpRequest(signUpForm, "123", "123"))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining(ApiErrorCode.INCORRECT_PASSWORD.getMessage());

    }


    @Test
    void deleteSignUpRequest() {
        SignUpForm signUpForm = SignUpForm.builder()
                .id("123")
                .password("123")
                .email("456")
                .name("테스트")
                .phoneNumber("01012345678")
                .identityPhoto("idCard.jpg")
                .studentCouncilFeePhoto("councilFee.jpg")
                .accountNumber("123")
                .build();

        given(signUpRequestRepository.findById("123")).willReturn(signUpForm);
        given(passwordEncoder.matches("123", "123")).willReturn(true);
        signUpRequestService.deleteSignUpRequest("123", "123");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(signUpRequestRepository).deleteSignUpRequest(captor.capture(), eq("123"));

        assertThat(captor.getValue()).isEqualTo("123");
        assertThatThrownBy(() -> signUpRequestService.deleteSignUpRequest("123", "345"))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining(ApiErrorCode.INCORRECT_PASSWORD.getMessage());
    }
}