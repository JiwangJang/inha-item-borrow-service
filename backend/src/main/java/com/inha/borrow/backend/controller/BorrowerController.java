package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SearchType;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerCacheData;
import com.inha.borrow.backend.model.dto.user.borrower.UpdateAccountNumberDto;
import com.inha.borrow.backend.model.dto.user.borrower.UpdateBanDto;
import com.inha.borrow.backend.model.dto.user.borrower.UpdatePhonenumberDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
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
    public ResponseEntity<ApiResponse<List<BorrowerCacheData>>> searchBorrower(@RequestParam("keyword") String keyword,
            @RequestParam("searchType") String searchType) {
        List<BorrowerCacheData> result = borrowerService.searchBorrowerCache(keyword, SearchType.valueOf(searchType));
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    /**
     * 현재 접속한 대여자의 정보를 불러오는 메서드
     * 
     * @return 200 요청 성공
     * @author 형민재
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<BorrowerCacheData>> findCacheById(
            @AuthenticationPrincipal Borrower borrower) {
        BorrowerCacheData foundedBorrower = borrowerService.findCacheById(borrower);
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
    public ResponseEntity<Void> updatePhoneNumber(@AuthenticationPrincipal Borrower borrower,
            @RequestBody UpdatePhonenumberDto dto) {
        borrowerService.updatePhoneNumber(borrower, dto);
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
    public ResponseEntity<Void> updateAccountNumber(@AuthenticationPrincipal Borrower borrower,
            @Valid @RequestBody UpdateAccountNumberDto dto) {
        borrowerService.updateAccountNumber(borrower, dto);
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
            @RequestBody UpdateBanDto dto) {
        if (dto.isBan() && (dto.getBanReason() == null || dto.getBanReason().isBlank())) {
            throw new InvalidValueException(ApiErrorCode.INVALID_VALUE.name(), "차단 사유를 입력해주세요.");
        }
        Borrower borrower = Borrower.builder().id(borrowerId).build();
        borrowerService.updateBan(borrower, dto);
        return ResponseEntity.ok().build();
    }
}
