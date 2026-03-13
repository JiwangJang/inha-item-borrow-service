package com.inha.borrow.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.UpdateStudentCouncilFeeVerificationDenyDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.UpdateStudentCouncilFeeVerificationPermitDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.UpdateStudentCouncilFeeVerificationResponseDto;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.service.StudentCouncilFeeVerificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student-council-fee-verification")
public class StuentCouncilFeeVerificationController {
    private final StudentCouncilFeeVerificationService service;

    // --------- 등록 메서드 ---------

    /**
     * 대여자가 인증 신청을 등록하는 메서드(거절후 재신청도 여기로)
     * 
     * @param verificationImage 인증이미지
     * @param borrower          사용자정보
     * @return 200
     * @author 장지왕
     */
    @PostMapping
    public ResponseEntity<Void> saveStudentCouncilFeeVerification(
            @RequestParam("verificationImage") MultipartFile verificationImage,
            @AuthenticationPrincipal Borrower borrower) {
        service.verificationRequestSave(borrower, verificationImage);
        return ResponseEntity.ok().build();
    }

    // --------- 조회 메서드 ---------

    /**
     * 관리자가 처리해야할(또는 한) 요청 목록을 가져오는 메서드
     * 
     * @return 학생회비 납부 인증 요청 목록 / 200
     * @author 장지왕
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentCouncilFeeVerification>>> findAll() {
        List<StudentCouncilFeeVerification> result = service.findAll();
        return ResponseEntity.ok(new ApiResponse<List<StudentCouncilFeeVerification>>(true, result));
    }

    /**
     * 인증 요청 목록 단건조회
     * 
     * @return 학생회비 납부 인증 요청 목록 / 200
     * @author 장지왕
     */
    @GetMapping("/single")
    public ResponseEntity<ApiResponse<StudentCouncilFeeVerification>> findByBorrowerId(
            @AuthenticationPrincipal Borrower borrower) {
        StudentCouncilFeeVerification result = service.findByBorrowerId(borrower);
        return ResponseEntity.ok(new ApiResponse<StudentCouncilFeeVerification>(true, result));
    }

    // --------- 수정 메서드 ---------

    /**
     * 대여자의 인증신청 요청을 승인하는 메서드
     * 
     * @param dto
     * @return 200
     * @author 장지왕
     */
    @PatchMapping("/{id}/permit")
    public ResponseEntity<Void> updateStudentCouncilFeeVerificationPermit(@PathVariable("id") String id,
            @RequestBody UpdateStudentCouncilFeeVerificationPermitDto dto) {
        service.updateStudentCouncilFeeVerificationPermit(dto, Integer.parseInt(id));
        return ResponseEntity.ok().build();
    }

    /**
     * 대여자의 인증신청을 거절하는 메서드
     * 
     * @param dto
     * @return
     */
    @PatchMapping("/{id}/deny")
    public ResponseEntity<Void> updateStudentCouncilFeeVerificationDeny(@PathVariable("id") String id,
            @RequestBody @Valid UpdateStudentCouncilFeeVerificationDenyDto dto) {
        service.updateStudentCouncilFeeVerificationDeny(dto, Integer.parseInt(id));
        return ResponseEntity.ok().build();
    }

    /**
     * 관리자가 인증 승인(거절)한거 수정하는 메서드
     * 
     * @param dto
     * @return 200
     * @author 장지왕
     */
    @PatchMapping("/{id}/modify")
    public ResponseEntity<Void> updateStudentCouncilFeeVerificationResponse(@PathVariable("id") String id,
            @Valid UpdateStudentCouncilFeeVerificationResponseDto dto) {
        service.updateStudentCouncilFeeVerificationResponse(dto, Integer.parseInt(id));
        return ResponseEntity.ok().build();
    }

    // --------- 삭제 메서드 ---------
    /**
     * 대여자가 인증 신청한거 취소하는 메서드(Soft-delete)
     * 
     * @param borrower
     * @return 204
     * @author 장지왕
     */
    @DeleteMapping
    public ResponseEntity<Void> updateStudentCouncilFeeVerificationCancel(@AuthenticationPrincipal Borrower borrower) {
        service.updateStudentCouncilFeeVerificationCancel(borrower);
        return ResponseEntity.noContent().build();
    }
}
