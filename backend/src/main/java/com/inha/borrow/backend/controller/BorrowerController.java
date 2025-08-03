package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.PatchPasswordDto;
import com.inha.borrow.backend.model.response.ApiResponse;
import com.inha.borrow.backend.model.user.Borrower;
import com.inha.borrow.backend.service.BorrowerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *대여자 관리 컨트롤러입니다.
 * 검색,수정을 담당합니다.

 * @author 형민재
 */

@AllArgsConstructor
@RestController
public class BorrowerController {
    private BorrowerService borrowerService;
    /**
     *대여자목록을 불러오는 메서드
     * @return 200 요청 성공
     * @author 형민재
     */
    @GetMapping("/borrowers")
    public ResponseEntity<ApiResponse<List<Borrower>>> findAllBorrower() {
        List<Borrower> borrower = borrowerService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, borrower));
    }

    /**
     *대여자목록을 id로 불러오는 메서드
     * @return 200 요청 성공
     * @author 형민재
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Borrower>> findById(@AuthenticationPrincipal String id) {
        Borrower borrower = borrowerService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, borrower));
    }

    /**
     *password를 수정하는 메서드
     * @param patchPasswordDto
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/info/password")
    public ResponseEntity<ApiResponse<Void>> patchPassword(@RequestBody PatchPasswordDto patchPasswordDto, @AuthenticationPrincipal String id){
        borrowerService.patchPassword(patchPasswordDto, id);
        return ResponseEntity.ok().build();

    }

    /**
     *email을 수정하는 메서드
     * @param email
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/info/email")
    public ResponseEntity<Void> patchEmail(@AuthenticationPrincipal String id, @RequestBody String email) {
        borrowerService.patchEmail(id, email);
        return ResponseEntity.ok().build();
    }

    /**
     *name을 수정하는 메서드
     * @param name
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/info/name")
    public ResponseEntity<Void> patchName(@AuthenticationPrincipal String id, @RequestBody String name) {
        borrowerService.patchName(id, name);
        return ResponseEntity.ok().build();
    }

    /**
     *phoneNumber를 수정하는 메서드
     * @param phoneNumber
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/info/phonenum")
    public ResponseEntity<Void> patchPhoneNumber(@AuthenticationPrincipal String id, @RequestBody String phoneNumber) {
        borrowerService.patchPhoneNumber(phoneNumber, id);
        return ResponseEntity.ok().build();
    }

    /**
     *ban을 수정하는 메서드
     * @param borrowerId
     * @param ban
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/{borrower-id}/info/ban")
    public ResponseEntity<Void> patchBan(@PathVariable("borrower-id") String borrowerId, @RequestBody boolean ban) {
        borrowerService.patchBan(ban, borrowerId);
        return ResponseEntity.ok().build();
    }
}
