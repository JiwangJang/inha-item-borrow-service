package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.EvaluationRequest;
import com.inha.borrow.backend.model.dto.SignUpForm;
import com.inha.borrow.backend.model.response.ApiResponse;
import com.inha.borrow.backend.service.S3Service;
import com.inha.borrow.backend.service.SignUpRequestService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *signUpRequest를 관리하는 컨트롤러
 * 회원가입, 정보 수정, 요청 삭제등을 담당합니다.
 * @author 형민재
 */

@RestController
@AllArgsConstructor
public class SignUpRequestController {
    private final S3Service s3Service;
    private final SignUpRequestService signUpRequestService;

    /**
     *회원가입을 진행하는 메서드
     * @param signUpForm
     * @param studentCouncilFdd
     * @param studentIdentification
     * @return 201 생성성공
     * @author 형민재
     */
    @PostMapping(value = "/borrowers/signup-requests",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SignUpForm>> signUpBorrower(@RequestPart SignUpForm signUpForm, @RequestPart MultipartFile studentIdentification, @RequestPart MultipartFile studentCouncilFdd) {
        String idCard = s3Service.uploadFile(studentIdentification, "student-identification");
        String councilFdd = s3Service.uploadFile(studentCouncilFdd, "student-council-fdd");
        signUpForm.setIdentityPhoto(idCard);
        signUpForm.setStudentCouncilFeePhoto(councilFdd);
        SignUpForm result = signUpRequestService.saveSignUpRequest(signUpForm);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, result));

    }
    /**
     *회원가입 요청을 승인하는 메서드
     * @param evaluationRequest
     * @param id
     * @return 200 생성성공
     * @author 형민재
     */
    @PutMapping("/borrowers/signup-request/{signup-request-id}")
    public ResponseEntity<ApiResponse<Void>> evaluateRequest(@RequestBody EvaluationRequest evaluationRequest, @PathVariable("signup-request-id") String id) {
        signUpRequestService.updateStateAndCreateBorrower(evaluationRequest, id);
        return ResponseEntity.ok().build();
    }

    /**
     *회원가입 요청을 수정하는 메서드
     * @param signUpForm
     * @param id
     * @param studentCouncilFdd
     * @param studentIdentification
     * @return 200 생성성공
     * @author 형민재
     */
    @PutMapping(value = "/signup-requests/{signup-request-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> rewriteRequest(@PathVariable("signup-request-id") String id, @RequestPart SignUpForm signUpForm,@RequestPart MultipartFile studentIdentification, @RequestPart MultipartFile studentCouncilFdd) {
        String idCard = s3Service.uploadFile(studentIdentification, "student-identification");
        String councilFdd = s3Service.uploadFile(studentCouncilFdd, "student-council-fdd");
        signUpForm.setIdentityPhoto(idCard);
        signUpForm.setStudentCouncilFeePhoto(councilFdd);
        signUpRequestService.patchSignUpRequest(signUpForm, id);
        return ResponseEntity.ok().build();
    }
    /**
     *회원가입 요청을 삭제하는 메서드
     * @param id
     * @return 204 요청성공
     * @author 형민재
     */

    @DeleteMapping("/signup-request/{signup-request-id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable("signup-request-id") String id, @RequestBody String password) {
        signUpRequestService.deleteSignUpRequest(id, password);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
