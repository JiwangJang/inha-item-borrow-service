package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.dto.signUpRequest.SignUpRequestPasswordDto;
import com.inha.borrow.backend.model.dto.user.borrower.SignUpFormDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.service.SignUpRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * signUpRequest를 관리하는 컨트롤러
 * 회원가입, 정보 수정, 요청 삭제등을 담당합니다.
 * 
 * @author 형민재
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/borrowers/signup-requests")
public class SignUpRequestController {
    private final SignUpRequestService signUpRequestService;

    /**
     * 회원가입 신청서 전체를 조회하는 메서드
     * 
     * @author 장지왕
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SignUpForm>>> findAllSignUpRequest() {
        List<SignUpForm> signUpForms = signUpRequestService.findSignUpRequest();
        ApiResponse<List<SignUpForm>> apiResponse = new ApiResponse<>(false, signUpForms);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 회원가입 신청 정보를 조회하는 메서드
     * 
     * @param param
     * @return
     */
    @GetMapping("/{signup-request-id}")
    public ResponseEntity<ApiResponse<SignUpForm>> findBySignUpRequestId(
            @AuthenticationPrincipal Admin admin,
            @PathVariable("signup-request-id") String signUpRequestId,
            @RequestBody SignUpRequestPasswordDto signUpRequestPasswordDto) {
        SignUpForm signUpForm = signUpRequestService.findById(admin,
                signUpRequestId,
                signUpRequestPasswordDto.getPassword());
        ApiResponse<SignUpForm> apiResponse = new ApiResponse<SignUpForm>(true, signUpForm);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 회원가입 신청하는 메서드
     * 
     * @param signUpFormDto
     * @param studentCouncilFee
     * @param studentIdentification
     * @return 201 생성성공
     * @author 형민재
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SignUpForm>> signUpBorrower(
            @Valid @RequestPart("signUpFormDto") SignUpFormDto signUpFormDto,
            @RequestPart("student-identification") MultipartFile studentIdentification,
            @RequestPart("student-council-fee") MultipartFile studentCouncilFee) {
        SignUpForm result = signUpRequestService.saveSignUpRequest(signUpFormDto, studentIdentification,
                studentCouncilFee);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, result));
    }

    /**
     * 회원가입 요청을 승인하는 메서드
     * 
     * @param EvaluationRequestDto
     * @param id
     * @return 200 생성성공
     * @author 형민재
     */
    @PatchMapping("/{signup-request-id}")
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
     * @param studentCouncilFee
     * @param studentIdentification
     * @return 200 생성성공
     * @author 형민재
     */
    @PutMapping(value = "/{signup-request-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> rewriteRequest(@PathVariable("signup-request-id") String id,
            @RequestPart("originPassword") String originPassword,
            @RequestPart("signUpForm") SignUpForm signUpForm,
            @RequestPart(value = "student-identification", required = false) MultipartFile studentIdentification,
            @RequestPart(value = "student-council-fee", required = false) MultipartFile studentCouncilFee) {
        signUpRequestService.patchSignUpRequest(signUpForm, studentIdentification, studentCouncilFee, id,
                originPassword);
        return ResponseEntity.noContent().build();
    }

    /**
     * 회원가입 요청을 삭제하는 메서드
     * 
     * @param id
     * @return 204 요청성공
     * @author 형민재
     */
    @DeleteMapping("/{signup-request-id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable("signup-request-id") String id,
            @RequestBody String password) {
        signUpRequestService.deleteSignUpRequest(id, password);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
