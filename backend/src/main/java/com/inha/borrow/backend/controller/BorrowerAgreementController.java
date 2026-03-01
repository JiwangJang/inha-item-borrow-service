package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.agreement.AgreementDto;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.entity.BorrowerAgreement;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.service.BorrowerAgreementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/agreement")
@RequiredArgsConstructor
public class BorrowerAgreementController {
    private final BorrowerAgreementService borrowerAgreementService;

    /**
     * 개인정보동의를 불러오기 위한 메서드
     *
     * @author 형민재
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BorrowerAgreement>>> getAllAgreement() {
        List<BorrowerAgreement> result = borrowerAgreementService.findAllAgreement();
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    /**
     * 개인정보동의를 borrowerId로 불러오는 메서드
     *
     * @author 형민재
     */
    @GetMapping("/borrower/{id}")
    public ResponseEntity<ApiResponse<List<BorrowerAgreement>>> getAgreementById(@PathVariable String id) {
        List<BorrowerAgreement> result = borrowerAgreementService.findByBorrower(id);
        return ResponseEntity.ok(new ApiResponse<List<BorrowerAgreement>>(true, result));
    }

    /**
     * 개인정보동의를 version으로 불러오는 메서드
     *
     * @param version
     * @author 형민재
     */
    @GetMapping("/version/{version}")
    public ResponseEntity<ApiResponse<List<BorrowerAgreement>>> getAgreementByVersion(@PathVariable String version) {
        List<BorrowerAgreement> result = borrowerAgreementService.findByVersion(version);
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    /**
     * 개인정보동의를 저장하기 위한 메서드
     *
     * @param user
     * @param agreementDto
     * @author 형민재
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Integer>> saveAgreement(@AuthenticationPrincipal Borrower user,
            @RequestBody @Valid AgreementDto agreementDto) {
        int result = borrowerAgreementService.saveAgreement(user.getId(), agreementDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, result));
    }

}
