package com.inha.borrow.backend.service;

import com.inha.borrow.backend.cache.IdCache;
import com.inha.borrow.backend.cache.SignUpSessionCache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.signUpRequest.SignUpRequestPasswordDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.SignUpFormDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.SignUpRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * signup_request table을 다루는 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignUpRequestService {
    private final PasswordEncoder passwordEncoder;
    private final SignUpRequestRepository signUpRequestRepository;
    private final BorrowerRepository borrowerRepository;
    private final JwtTokenService jwtTokenService;
    private final SignUpSessionCache signUpSessionCache;
    private final S3Service s3Service;
    private final IdCache idCache;

    @Value("${app.cloud.aws.s3.dir.student-council-fee}")
    private String STUDENT_COUNCIL_FEE_PATH;
    @Value("${app.cloud.aws.s3.dir.student-identification}")
    private String STUDENT_IDENTIFICATION_PATH;

    /**
     * signUpRequest를 저장하는 메서드
     *
     * @param signUpFormDto
     * @return 저장 정보
     * @author 형민재
     */
    public SignUpForm saveSignUpRequest(SignUpFormDto signUpFormDto, MultipartFile studentIdentification,
            MultipartFile studentCouncilFee) {
        if (signUpSessionCache.isAllPassed(signUpFormDto.getId())) {
            String encodedPassword = passwordEncoder.encode(signUpFormDto.getPassword());
            signUpFormDto.setPassword(encodedPassword);
            try {
                String idCard = s3Service.uploadFile(studentIdentification, STUDENT_IDENTIFICATION_PATH,
                        signUpFormDto.getId());
                String councilFee = s3Service.uploadFile(studentCouncilFee, STUDENT_COUNCIL_FEE_PATH,
                        signUpFormDto.getId());
                return signUpRequestRepository.save(signUpFormDto.getSignUpForm(idCard, councilFee));
            } catch (DataAccessException e) {
                s3Service.deleteAllFile("bucket", signUpFormDto.getId());
                throw e;
            }
        }
        ApiErrorCode errorCode = ApiErrorCode.SIGN_UP_PASS_FAILED;
        throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
    }

    /**
     * signUpRequest들을 반환하는 메서드
     *
     * @return request 목록
     * @author 형민재
     */
    public List<SignUpForm> findSignUpRequest() {
        return signUpRequestRepository.findAll();
    }

    /**
     * singUpRequest 단건조회
     *
     * @param id
     * @return
     */
    public SignUpForm findById(Admin admin, String signUpRequestId, SignUpRequestPasswordDto passwordDto) {
        if (admin != null) {
            return signUpRequestRepository.findById(signUpRequestId);
        } else {
            // 관리자가 아닐경우 비밀번호 확인 후 자신의 회원가입 신청만 조회 가능
            String encodedPassword = signUpRequestRepository.findPasswordById(signUpRequestId);
            if (passwordEncoder.matches(passwordDto.getPassword(), encodedPassword)) {
                return signUpRequestRepository.findById(signUpRequestId);
            } else {
                ApiErrorCode errorCode = ApiErrorCode.INVALID_PASSWORD;
                errorCode.setMessage("비밀번호가 다릅니다.");
                throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
            }
        }

    }

    /**
     * signUpRequest의 state rejection을 설정하는 메서드
     *
     * @param evaluationRequestDto
     * @param id
     * @return 수정 정보
     * @author 형민재
     */

    public void updateStateAndCreateBorrower(EvaluationRequestDto evaluationRequestDto, String id) {
        if (SignUpRequestState.PERMIT == evaluationRequestDto.getState()) {
            SignUpForm signUpForm = signUpRequestRepository.findById(id);
            BorrowerDto borrower = transition(signUpForm);
            borrower.setRefreshToken(jwtTokenService.createToken(id));
            s3Service.deleteAllFile("bucket", id);
            borrowerRepository.save(borrower);
        }
        signUpRequestRepository.patchEvaluation(evaluationRequestDto, id);
    }

    /**
     * signUpRequest를 수정하는 메서드
     *
     * @param signUpForm
     * @param id
     * @return 수정 정보
     * @author 형민재
     */
    public void patchSignUpRequest(SignUpForm signUpForm, MultipartFile studentIdentification,
            MultipartFile studentCouncilFee, String id, String originPassword) {
        if (!idCache.contains(id)) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            errorCode.setMessage("기존 회원가입 요청을 찾을 수 없습니다.");
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
        String password = signUpRequestRepository.findPasswordById(id);
        if (passwordEncoder.matches(originPassword, password)) {
            String encodedPassword = passwordEncoder.encode(signUpForm.getPassword());
            signUpForm.setPassword(encodedPassword);
            if (studentCouncilFee != null && !studentCouncilFee.isEmpty()) {
                String councilFee = s3Service.uploadFile(studentCouncilFee,
                        "student-council-fee", id);
                signUpForm.setStudentCouncilFeePhoto(councilFee);
            }
            if (studentIdentification != null && !studentIdentification.isEmpty()) {
                String idCard = s3Service.uploadFile(studentIdentification,
                        "student-identification", id);
                signUpForm.setIdentityPhoto(idCard);
            }
            signUpRequestRepository.patchSignUpRequest(signUpForm, id);
        } else {
            ApiErrorCode errorCode = ApiErrorCode.INCORRECT_PASSWORD;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * signUpRequest를 수정하는 메서드
     *
     * @param id
     * @author 형민재
     */
    public void deleteSignUpRequest(String id, String password) {
        SignUpForm signUpForm = signUpRequestRepository.findById(id);
        if (passwordEncoder.matches(password, signUpForm.getPassword())) {
            signUpRequestRepository.deleteSignUpRequest(id, password);
        } else {
            ApiErrorCode errorCode = ApiErrorCode.INCORRECT_PASSWORD;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
    }

    protected BorrowerDto transition(SignUpForm signUpForm) {
        return new BorrowerDto(
                signUpForm.getId(),
                signUpForm.getPassword(),
                signUpForm.getEmail(),
                signUpForm.getName(),
                signUpForm.getPhonenumber(),
                signUpForm.getIdentityPhoto(),
                signUpForm.getAccountNumber(),
                null);
    }
}
