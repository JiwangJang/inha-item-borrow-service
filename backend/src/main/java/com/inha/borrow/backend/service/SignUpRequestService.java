package com.inha.borrow.backend.service;
import com.inha.borrow.backend.enums.EvaluateSignUP;
import com.inha.borrow.backend.model.dto.BorrowerDto;
import com.inha.borrow.backend.model.dto.EvaluationRequest;
import com.inha.borrow.backend.model.dto.SignUpForm;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.SignUpRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * signup_request table을 다루는 클래스
 */
@Service
@AllArgsConstructor
public class SignUpRequestService {

    private PasswordEncoder passwordEncoder;
    private SignUpRequestRepository signUpRequestRepository;
    private BorrowerRepository borrowerRepository;
    private JwtTokenService jwtTokenService;


    /**
     *signUpRequest를 저장하는 메서드
     *
     * @param signUpForm
     * @return 저장 정보
     * @author 형민재
     */
    public SignUpForm saveSignUpRequest(SignUpForm signUpForm){
        String encodedPassword = passwordEncoder.encode(signUpForm.getPassword());
        signUpForm.setPassword(encodedPassword);
        return signUpRequestRepository.save(signUpForm);
    }

    /**
     *signUpRequest들을 반환하는 메서드
     *
     * @return request 목록
     * @author 형민재
     */
    public List<SignUpForm>findSignRequest(){
        return signUpRequestRepository.findAll();
    }

    /**
     *signUpRequest의 state rejection을 설정하는 메서드
     *
     * @param evaluationRequest
     * @param id
     * @return 수정 정보
     * @author 형민재
     */

    public void updateStateAndCreateBorrower(EvaluationRequest evaluationRequest, String id){
        if(EvaluateSignUP.PERMIT.equals(evaluationRequest.getState())){
            SignUpForm signUpForm = signUpRequestRepository.findById(id);
                BorrowerDto borrower = transition(signUpForm);
                borrower.setRefreshToken(jwtTokenService.createToken(id));
                borrowerRepository.save(borrower);
        }
        signUpRequestRepository.patchEvaluation(evaluationRequest,id);
    }

    /**
     *signUpRequest를 수정하는 메서드
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
     *signUpRequest를 수정하는 메서드
     *
     * @param id
     * @author 형민재
     */
    public void deleteSignUpRequest(String id, String password){
        SignUpForm signUpForm = signUpRequestRepository.findById(id);
        if(passwordEncoder.matches(signUpForm.getPassword(), password)) {
            signUpRequestRepository.deleteSignUpRequest(id, password);
        }
    }
    public BorrowerDto transition(SignUpForm signUpForm){
        return new BorrowerDto(
                signUpForm.getId(),
                signUpForm.getPassword(),
                signUpForm.getEmail(),
                signUpForm.getName(),
                signUpForm.getPhoneNumber(),
                signUpForm.getIdentityPhoto(),
                signUpForm.getAccountNumber(),
                null
        );
    }

}

