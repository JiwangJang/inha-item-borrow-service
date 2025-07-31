package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.response.ApiResponse;
import com.inha.borrow.backend.model.user.Borrower;
import com.inha.borrow.backend.service.BorrowerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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
     * @param borrowerId
     * @return 200 요청 성공
     * @author 형민재
     */
    @GetMapping("/{borrower-id}/info")
    public ResponseEntity<ApiResponse<Borrower>> findById(@PathVariable("borrower-id") String borrowerId) {
        Borrower borrower = borrowerService.findById(borrowerId);
        return ResponseEntity.ok(new ApiResponse<>(true, borrower));
    }

    /**
     *password를 수정하는 메서드
     * @param borrowerId
     * @param password
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/{borrower-id}/info/password")
    public ResponseEntity<ApiResponse<Void>> patchPassword(@RequestBody String password, @PathVariable("borrower-id") String borrowerId) {
        borrowerService.patchPassword(password, borrowerId);
        return ResponseEntity.ok().build();

    }

    /**
     *password를 수정하는 메서드
     * @param borrowerId
     * @param email
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/{borrower-id}/info/email")
    public ResponseEntity<Void> patchEmail(@PathVariable("borrower-id") String borrowerId, @RequestBody String email) {
        borrowerService.patchEmail(borrowerId, email);
        return ResponseEntity.ok().build();
    }

    /**
     *password를 수정하는 메서드
     * @param borrowerId
     * @param name
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/{borrower-id}/info/name")
    public ResponseEntity<Void> patchName(@PathVariable("borrower-id") String borrowerId, @RequestBody String name) {
        borrowerService.patchName(borrowerId, name);
        return ResponseEntity.ok().build();
    }

    /**
     *password를 수정하는 메서드
     * @param borrowerId
     * @param phoneNumber
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/{borrower-id}/info/phonenum")
    public ResponseEntity<Void> patchPhoneNumber(@PathVariable("borrower-id") String borrowerId, @RequestBody String phoneNumber) {
        borrowerService.patchPhoneNumber(borrowerId, phoneNumber);
        return ResponseEntity.ok().build();
    }

    /**
     *password를 수정하는 메서드
     * @param borrowerId
     * @param ban
     * @return 200 요청 성공
     * @author 형민재
     */
    @PutMapping("/{borrower-id}/info/ban")
    public ResponseEntity<Void> patchBan(@PathVariable("borrower-id") String borrowerId, @RequestBody int ban) {
        borrowerService.patchBan(ban, borrowerId);
        return ResponseEntity.ok().build();
    }
}
