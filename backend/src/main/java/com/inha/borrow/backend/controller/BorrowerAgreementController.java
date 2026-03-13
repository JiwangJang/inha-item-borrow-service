package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.agreement.AgreementDto;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.service.BorrowerAgreementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/agreement")
@RequiredArgsConstructor
public class BorrowerAgreementController {
    private final BorrowerAgreementService borrowerAgreementService;

    /**
     * 개인정보 동의여부를 저장하기 위한 메서드
     *
     * @param user
     * @param agreementDto
     * @author 형민재
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Integer>> saveAgreement(@AuthenticationPrincipal Borrower borrower,
            @RequestBody @Valid AgreementDto agreementDto) {
        int result = borrowerAgreementService.saveAgreement(borrower, agreementDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, result));
    }
}
