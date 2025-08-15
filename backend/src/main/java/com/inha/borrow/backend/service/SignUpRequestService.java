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
import lombok.RequiredArgsConstructor;
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
public class SignUpRequestService {
    private final PasswordEncoder passwordEncoder;
    private final SignUpRequestRepository signUpRequestRepository;
    private final BorrowerRepository borrowerRepository;
    private final JwtTokenService jwtTokenService;
    private final SignUpSessionCache signUpSessionCache;
    private final S3Service s3Service;

    @Value("${app.cloud.aws.s3.dir.name}")
    private String STUDENT_COUNCIL_FEE_PATH;
    @Value("${app.cloud.aws.s3.dir.name}")
    private String STUDENT_IDENTIFICATION_PATH;


    /**
     * signUpRequest를 저장하는 메서드
     *
     * @param signUpForm
     * @return 저장 정보
     * @author 형민재
     */

    public SignUpForm saveSignUpRequest(SignUpFormDto signUpForm, MultipartFile studentIdentification, MultipartFile studentCouncilFee) {
        if (signUpSessionCache.isAllPassed(signUpForm.getId())) {
            String encodedPassword = passwordEncoder.encode(signUpForm.getPassword());
            signUpForm.setPassword(encodedPassword);
            signUpSessionCache.get(signUpForm.getId());
            try{
                String idCard = s3Service.uploadFile(studentIdentification, STUDENT_IDENTIFICATION_PATH, signUpForm.getId());
                String councilFee = s3Service.uploadFile(studentCouncilFee, STUDENT_COUNCIL_FEE_PATH, signUpForm.getId());
                return signUpRequestRepository.save(signUpForm.getSignUpForm(idCard,councilFee));
        }catch (DataAccessException e){
                s3Service.deleteAllFile("bucket",signUpForm.getId());
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
    public SignUpForm findById(String id){
        return signUpRequestRepository.findById(id);
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
            s3Service.deleteAllFile("bucket",id);
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
    public void patchSignUpRequest(SignUpForm signUpForm, String id, String originPassword) {
        String password = signUpRequestRepository.findPasswordById(id);
        if (passwordEncoder.matches(originPassword, password)) {
            String encodedPassword = passwordEncoder.encode(signUpForm.getPassword());
            signUpForm.setPassword(encodedPassword);
            signUpRequestRepository.patchSignUpRequest(signUpForm, id);
        }else {
            ApiErrorCode errorCode = ApiErrorCode.INCORRECT_PASSWORD;
            throw new InvalidValueException(errorCode.name(),errorCode.getMessage());
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
        if (passwordEncoder.matches(signUpForm.getPassword(), password)) {
            signUpRequestRepository.deleteSignUpRequest(id, password);
        } else {
            ApiErrorCode errorCode = ApiErrorCode.INCORRECT_PASSWORD;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
    }

    private BorrowerDto transition(SignUpForm signUpForm) {
        return new BorrowerDto(
                signUpForm.getId(),
                signUpForm.getPassword(),
                signUpForm.getEmail(),
                signUpForm.getName(),
                signUpForm.getPhoneNumber(),
                signUpForm.getIdentityPhoto(),
                signUpForm.getAccountNumber(),
                null);
    }
}
