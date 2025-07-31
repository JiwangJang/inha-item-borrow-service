package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.EvaluationRequest;
import com.inha.borrow.backend.model.dto.SignUpForm;
import com.inha.borrow.backend.model.response.ApiResponse;
import com.inha.borrow.backend.service.SignUpRequestService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 *signUpRequest를 관리하는 컨트롤러
 * 회원가입, 정보 수정, 요청 삭제등을 담당합니다.
 * @author 형민재
 */

@RestController
@AllArgsConstructor
public class SignUpRequestController {
    private SignUpRequestService signUpRequestService;

    /**
     *회원가입을 진행하는 메서드
     * @param signUpForm
     * @return 201 생성성공
     * @author 형민재
     */
    @PostMapping("/borrowers/signup-requests")
    public ResponseEntity<ApiResponse<SignUpForm>> signUpBorrower(@RequestBody SignUpForm signUpForm) {
        SignUpForm result = signUpRequestService.saveSignUpRequest(signUpForm);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true,result));
    }

    /**
     *회원가입 요청을 승인하는 메서드
     * @param evaluationRequest
     * @param id
     * @return 200 생성성공
     * @author 형민재
     */
    @PutMapping("/borrowers/signup-request/{signup-request-id}")
    public ResponseEntity<ApiResponse<Integer>> evaluateRequest(@RequestBody EvaluationRequest evaluationRequest, @PathVariable("signup-request-id") String id) {
        int result = signUpRequestService.updateStateAndCreateBorrower(evaluationRequest, id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, result));
    }

    /**
     *회원가입 요청을 수정하는 메서드
     * @param signUpForm
     * @param id
     * @return 200 생성성공
     * @author 형민재
     */
    @PutMapping("/signup-requests/{signup-request-id}")
    public ResponseEntity<ApiResponse<SignUpForm>> rewriteRequest(@PathVariable("signup-request-id") String id, @RequestBody SignUpForm signUpForm) {
        SignUpForm result = signUpRequestService.patchSignUpRequest(signUpForm, id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, result));
    }
    /**
     *회원가입 요청을 삭제하는 메서드
     * @param id
     * @return 204 요청성공
     * @author 형민재
     */

    @DeleteMapping("/signup-request/{signup-request-id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable("signup-request-id") String id) {
        signUpRequestService.deleteSignUpRequest(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
