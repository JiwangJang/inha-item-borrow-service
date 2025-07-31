package com.inha.borrow.backend.service;
import com.inha.borrow.backend.model.dto.BorrowerDto;
import com.inha.borrow.backend.model.dto.EvaluationRequest;
import com.inha.borrow.backend.model.dto.SignUpForm;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.SignUpRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * signup_request table을 다루는 클래스
 */
@Service
@AllArgsConstructor
public class SignUpRequestService {

    private SignUpRequestRepository signUpRequestRepository;
    private BorrowerRepository borrowerRepository;


    /**
     *signUpRequest를 저장하는 메서드
     *
     * @param signUpForm
     * @return 저장 정보
     * @author 형민재
     */
    public SignUpForm saveSignUpRequest(SignUpForm signUpForm){
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

    public int updateStateAndCreateBorrower(EvaluationRequest evaluationRequest, String id){
        if("PERMIT".equals(evaluationRequest.getState())){
            SignUpForm signUpForm = signUpRequestRepository.findById(id);
            if(signUpForm != null){
                BorrowerDto borrower = transition(signUpForm);
                borrowerRepository.save(borrower);
            }
        }
        return signUpRequestRepository.patchEvaluation(evaluationRequest,id);
    }

    /**
     *signUpRequest를 수정하는 메서드
     *
     * @param signUpForm
     * @param id
     * @return 수정 정보
     * @author 형민재
     */
    public SignUpForm patchSignUpRequest(SignUpForm signUpForm, String id){
        return signUpRequestRepository.patchSignUpRequest(signUpForm,id);
    }

    /**
     *signUpRequest를 수정하는 메서드
     *
     * @param id
     * @author 형민재
     */
    public void deleteSignUpRequest(String id){
        signUpRequestRepository.deleteSignUpRequest(id);

    }
    public BorrowerDto transition(SignUpForm signUpForm){
        return new BorrowerDto(
                signUpForm.getId(),
                signUpForm.getPassword(),
                signUpForm.getEmail(),
                signUpForm.getName(),
                signUpForm.getPhoneNumber(),
                signUpForm.getIdentityPhoto(),
                signUpForm.getAccountNumber()
        );
    }

}

