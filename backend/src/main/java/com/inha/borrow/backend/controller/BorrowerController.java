package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SearchType;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchAccountNumberDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchBanDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchPhonenumberDto;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.service.BorrowerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 대여자 관리 컨트롤러입니다.
 * 검색,수정을 담당합니다.
 * 
 * @author 형민재
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/borrowers")
@Slf4j
public class BorrowerController {
    private final BorrowerService borrowerService;

    // --------- 조회메서드 ---------
    /**
     * 대여자 검색하는 메서드
     * 
     * @return 200 요청 성공
     * @author 장지왕
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CacheBorrowerDto>>> searchBorrower(@RequestParam("keyword") String keyword,
            @RequestParam("searchType") String searchType) {
        List<CacheBorrowerDto> result = borrowerService.searchBorrower(keyword, SearchType.valueOf(searchType));
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    /**
     * 현재 접속한 대여자의 정보를 불러오는 메서드
     * 
     * @return 200 요청 성공
     * @author 형민재
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<CacheBorrowerDto>> findCacheById(
            @AuthenticationPrincipal(expression = "id") String id) {
        CacheBorrowerDto foundedBorrower = borrowerService.findCacheById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, foundedBorrower));
    }

    // --------- 수정메서드 ---------

    /**
     * phoneNumber를 수정하는 메서드
     * 
     * @param id
     * @return 200 요청 성공
     * @author 형민재
     */
    @PatchMapping("/info/phonenum")
    public ResponseEntity<Void> updatePhoneNumber(@AuthenticationPrincipal(expression = "id") String id,
            @RequestBody PatchPhonenumberDto dto) {
        borrowerService.patchPhoneNumber(id, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * accountNumber을 수정하는 메서드
     *
     * @param accountNumber
     * @param borrowerId
     * @return 200 요청 성공
     * @author 형민재
     */
    @PatchMapping("/info/account-num")
    public ResponseEntity<Void> updateAccountNumber(@AuthenticationPrincipal(expression = "id") String borrowerId,
            @Valid @RequestBody PatchAccountNumberDto dto) {
        borrowerService.patchAccountNumber(dto.getNewAccountNumber(), borrowerId);
        return ResponseEntity.ok().build();
    }

    // --------- 삭제메서드 ---------

    /**
     * 금지여부를 수정하는 메서드
     * 
     * @param borrowerId
     * @param ban
     * @return 200 요청 성공
     * @author 형민재
     */
    @PatchMapping("/{borrower-id}/info/ban")
    public ResponseEntity<Void> updateBan(@PathVariable("borrower-id") String borrowerId,
            @RequestBody PatchBanDto dto) {
        if (dto.isBan() && (dto.getBanReason() == null || dto.getBanReason().isBlank())) {
            throw new InvalidValueException(ApiErrorCode.INVALID_VALUE.name(), "차단 사유를 입력해주세요.");
        }
        borrowerService.patchBan(dto, borrowerId);
        return ResponseEntity.ok().build();
    }
}
