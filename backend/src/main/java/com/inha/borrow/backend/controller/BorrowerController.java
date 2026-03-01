package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SearchType;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchAccountNumberDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchBanDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchPhonenumberDto;
import com.inha.borrow.backend.model.dto.user.borrower.SavePhoneAccountNumberDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.service.BorrowerService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    /**
     * 대여자목록을 불러오는 메서드
     * 
     * @return 200 요청 성공
     * @author 형민재
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Borrower>>> findAllBorrower() {
        List<Borrower> borrower = borrowerService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, borrower));
    }

    /**
     * 대여자 검색하는 거
     * 
     * @return 200 요청 성공
     * @author 형민재
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CacheBorrowerDto>>> searchBorrower(@RequestParam("keyword") String keyword,
            @RequestParam("searchType") String searchType) {
        List<CacheBorrowerDto> result = borrowerService.searchBorrower(keyword, SearchType.valueOf(searchType));
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    /**
     * 현재 유저의 정보 반환
     * 
     * @return 200 요청 성공
     * @author 장지왕
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CacheBorrowerDto>> me(@AuthenticationPrincipal Borrower borrower) {
        CacheBorrowerDto dto = borrowerService.getMyInfo(borrower.getId());

        return ResponseEntity.ok(new ApiResponse<>(true, dto));
    }

    /**
     * 대여자를 id로 불러오는 메서드
     * 
     * @return 200 요청 성공
     * @author 형민재
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Borrower>> findById(
            @AuthenticationPrincipal(expression = "id") String id) {
        Borrower foundedBorrower = borrowerService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, foundedBorrower));
    }

    /**
     * phoneNumber를 수정하는 메서드
     * 
     * @param id
     * @return 200 요청 성공
     * @author 형민재
     */
    @PatchMapping("/info/phonenum")
    public ResponseEntity<Void> patchPhoneNumber(@AuthenticationPrincipal(expression = "id") String id,
            @RequestBody PatchPhonenumberDto dto) {
        borrowerService.patchPhoneNumber(id, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * name을 수정하는 메서드
     * 
     * @param name
     * @param borrowerId
     * @return 200 요청 성공
     * @author 형민재
     */
    @PatchMapping("/{borrower-id}/info/name")
    public ResponseEntity<Void> patchName(@PathVariable("borrower-id") String borrowerId,
            @Valid @NotBlank @RequestBody String name) {
        // 이거 없어도 될듯(로그인 할때마다 이름 바뀌었나 확인하게 하면 되니)
        borrowerService.patchName(borrowerId, name);
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
    public ResponseEntity<Void> patchAccountNumber(@AuthenticationPrincipal(expression = "id") String borrowerId,
            @Valid @RequestBody PatchAccountNumberDto dto) {
        borrowerService.patchAccountNumber(dto.getNewAccountNumber(), borrowerId);
        return ResponseEntity.ok().build();
    }

    /**
     * user의 전화번호 계좌번호를 수정하는 메서드
     *
     * @param dto
     * @param borrowerId
     * @return 200 요청 성공
     * @author 형민재
     */
    @PatchMapping("/info/user-num")
    public ResponseEntity<Void> patchUserNumber(@AuthenticationPrincipal(expression = "id") String borrowerId,
            @Valid @NotBlank @RequestBody SavePhoneAccountNumberDto dto) {
        borrowerService.savePhoneAccountNumber(borrowerId, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * ban을 수정하는 메서드
     * 
     * @param borrowerId
     * @param ban
     * @return 200 요청 성공
     * @author 형민재
     */
    @PatchMapping("/{borrower-id}/info/ban")
    public ResponseEntity<Void> patchBan(@PathVariable("borrower-id") String borrowerId,
            @RequestBody PatchBanDto dto) {
        if (dto.isBan() && (dto.getBanReason() == null || dto.getBanReason().isBlank())) {
            throw new InvalidValueException(ApiErrorCode.INVALID_VALUE.name(), "차단 사유를 입력해주세요.");
        }
        borrowerService.patchBan(dto, borrowerId);
        return ResponseEntity.ok().build();
    }
}
