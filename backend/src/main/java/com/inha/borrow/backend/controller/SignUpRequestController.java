package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.SignUpFormDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.service.S3Service;
import com.inha.borrow.backend.service.SignUpRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * signUpRequest를 관리하는 컨트롤러
 * 회원가입, 정보 수정, 요청 삭제등을 담당합니다.
 * 
 * @author 형민재
 */

@RestController
@RequiredArgsConstructor
public class SignUpRequestController {
    private final S3Service s3Service;
    private final SignUpRequestService signUpRequestService;

    @Value("${app.cloud.aws.s3.dir.student-council-fee}")
    private String STUDENT_COUNCIL_FEE_PATH;
    @Value("${app.cloud.aws.s3.dir.student-identification}")
    private String STUDENT_IDENTIFICATION_PATH;

    /**
     * 회원가입을 진행하는 메서드
     * 
     * @param signUpForm
     * @param studentCouncilFdd
     * @param studentIdentification
     * @return 201 생성성공
     * @author 형민재
     */
    @PostMapping(value = "/borrowers/signup-requests", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ApiResponse<SignUpForm>> signUpBorrower(
            @Valid @RequestPart("signUpFormDto") SignUpFormDto signUpFormDto,
            @RequestPart("student-identification") MultipartFile studentIdentification,
            @RequestPart("student-council-fee") MultipartFile studentCouncilFee) {
        String idCard = s3Service.uploadFile(studentIdentification, STUDENT_IDENTIFICATION_PATH, signUpFormDto.getId());
        String councilFee = s3Service.uploadFile(studentCouncilFee, STUDENT_COUNCIL_FEE_PATH, signUpFormDto.getId());
        SignUpForm signUpForm = signUpFormDto.getSignUpForm(idCard, councilFee);
        SignUpForm result = signUpRequestService.saveSignUpRequest(signUpForm);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, result));
    }

    /**
     * 회원가입 요청을 승인하는 메서드
     * 
     * @param evaluationRequest
     * @param id
     * @return 200 생성성공
     * @author 형민재
     */
    @PutMapping("/borrowers/signup-request/{signup-request-id}")
    public ResponseEntity<ApiResponse<Void>> evaluateRequest(
            @Valid @RequestBody EvaluationRequestDto EvaluationRequestDto,
            @PathVariable("signup-request-id") String id) {
        signUpRequestService.updateStateAndCreateBorrower(EvaluationRequestDto, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 회원가입 요청을 수정하는 메서드
     * 
     * @param signUpForm
     * @param id
     * @param studentCouncilFdd
     * @param studentIdentification
     * @return 200 생성성공
     * @author 형민재
     */
    @PutMapping(value = "/signup-requests/{signup-request-id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ApiResponse<Void>> rewriteRequest(@PathVariable("signup-request-id") String id,
            @RequestPart SignUpForm signUpForm,
            @RequestPart(value = "student-identification", required = false) MultipartFile studentIdentification,
            @RequestPart(value = "student-council-fee", required = false) MultipartFile studentCouncilFee) {
        // 기존 파일 삭제하는 로직필요함 + 관리자 권한을 가졌다면 그냥 수정가능하게(또한 관리자만 이름 수정가능)
        // 사진 있을때 없을때 로직 분리 필요
        // String idCard = s3Service.uploadFile(studentIdentification,
        // "student-identification");
        // String councilFee = s3Service.uploadFile(studentCouncilFdd,
        // "student-council-fee");
        // signUpForm.setIdentityPhoto(idCard);
        // signUpForm.setStudentCouncilFeePhoto(councilFee);
        // signUpRequestService.patchSignUpRequest(signUpForm, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 회원가입 요청을 삭제하는 메서드
     * 
     * @param id
     * @return 204 요청성공
     * @author 형민재
     */

    @DeleteMapping("/signup-request/{signup-request-id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable("signup-request-id") String id,
            @RequestBody String password) {
        signUpRequestService.deleteSignUpRequest(id, password);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
