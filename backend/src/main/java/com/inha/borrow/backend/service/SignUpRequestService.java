package com.inha.borrow.backend.service;

import com.inha.borrow.backend.cache.SignUpSessionCache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.SignUpRequestRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    /**
     * signUpRequest를 저장하는 메서드
     *
     * @param signUpForm
     * @return 저장 정보
     * @author 형민재
     */
    public SignUpForm saveSignUpRequest(SignUpForm signUpForm) {
        if (signUpSessionCache.isAllPassed(signUpForm.getId())) {
            String encodedPassword = passwordEncoder.encode(signUpForm.getPassword());
            signUpForm.setPassword(encodedPassword);
            return signUpRequestRepository.save(signUpForm);
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
     * signUpRequest의 state rejection을 설정하는 메서드
     *
     * @param evaluationRequest
     * @param id
     * @return 수정 정보
     * @author 형민재
     */

    public void updateStateAndCreateBorrower(EvaluationRequestDto evaluationRequestDto, String id) {
        if (SignUpRequestState.PERMIT == evaluationRequestDto.getState()) {
            SignUpForm signUpForm = signUpRequestRepository.findById(id);
            BorrowerDto borrower = transition(signUpForm);
            borrower.setRefreshToken(jwtTokenService.createToken(id));
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
    public void patchSignUpRequest(SignUpForm signUpForm, String id) {
        SignUpForm signUpForm1 = signUpRequestRepository.findById(id);
        if (passwordEncoder.matches(signUpForm.getPassword(), signUpForm1.getPassword())) {
            String encodedPassword = passwordEncoder.encode(signUpForm.getPassword());
            signUpForm.setPassword(encodedPassword);
            signUpRequestRepository.patchSignUpRequest(signUpForm, id);
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
