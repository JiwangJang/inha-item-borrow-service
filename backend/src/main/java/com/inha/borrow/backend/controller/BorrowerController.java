package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.user.borrower.PatchPhonenumberDto;
import com.inha.borrow.backend.model.dto.user.borrower.SavePhoneAccountNumberDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
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
     * 대여자목록을 id로 불러오는 메서드
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
            @Valid @NotBlank @RequestBody String accountNumber) {
        borrowerService.patchAccountNumber(accountNumber,borrowerId);
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
        borrowerService.savePhoneAccountNumber(borrowerId,dto);
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
            @RequestBody String ban) {
        borrowerService.patchBan(Boolean.parseBoolean(ban), borrowerId);
        return ResponseEntity.ok().build();
    }
}
