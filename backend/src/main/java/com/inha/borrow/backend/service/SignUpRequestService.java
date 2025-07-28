package com.inha.borrow.backend.service;

import com.inha.borrow.backend.model.dto.BorrowerDto;
import com.inha.borrow.backend.model.dto.EvaluationRequest;
import com.inha.borrow.backend.model.dto.SignUpForm;
import com.inha.borrow.backend.model.user.Borrower;
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

    public SignUpForm saveSignUpRequest(SignUpForm signUpForm){
        SignUpForm save = signUpRequestRepository.save(signUpForm);
        if(save ==null){
            throw new RuntimeException("저장 실패");
        }
        return save;
    }
    public List<SignUpForm>findSignRequest(){
        return signUpRequestRepository.findAll();
    }

    public SignUpForm updateStateAndCreateBorrower(EvaluationRequest evaluationRequest, String id){
        SignUpForm save = signUpRequestRepository.patchEvaluation(evaluationRequest,id);
        if("PERMIT".equals(evaluationRequest.getState())){
            SignUpForm signUpForm = signUpRequestRepository.findById(id);
            if(signUpForm != null){
                BorrowerDto borrower = transition(signUpForm);
                borrowerRepository.save(borrower);
            }
        }
        return save;
    }
    public SignUpForm patchSignUpRequest(SignUpForm signUpForm, String id){
        SignUpForm save = signUpRequestRepository.patchSignUpRequest(signUpForm,id);
        if(save == null){
            throw new RuntimeException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return save;
    }
    public SignUpForm deleteSignUpRequest(String id){
        SignUpForm save = signUpRequestRepository.deleteSignUpRequest(id);
        if(save==null){
            throw new RuntimeException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return save;
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

