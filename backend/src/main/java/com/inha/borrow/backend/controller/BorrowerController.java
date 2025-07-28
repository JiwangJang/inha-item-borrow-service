package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.user.Borrower;
import com.inha.borrow.backend.service.BorrowerService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@AllArgsConstructor
@RestController
public class BorrowerController {
    private BorrowerService borrowerService;

    @GetMapping("/borrowers")
    public List<Borrower> findAllBorrower() {
        return borrowerService.findAll();
    }

    @GetMapping("/{borrower-id}/info")
    public ResponseEntity<Borrower> findById(@PathVariable("borrower-id") String borrowerId) {
        try {
            Borrower findId = borrowerService.findById(borrowerId);
            if (findId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(findId);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{borrower-id}/info/password")
    public ResponseEntity<Void> patchPassword(@RequestBody String password, @PathVariable("borrower-id") String borrowerId) {
        try {
            Borrower patchPw = borrowerService.patchPassword(password,borrowerId);
            if (patchPw == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/{borrower-id}/info/email")
    public ResponseEntity<Void> patchEmail(@PathVariable("borrower-id") String borrowerId, @RequestBody String email){
        try {
            Borrower patchPw = borrowerService.patchEmail(borrowerId, email);
            if (patchPw == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/{borrower-id}/info/name")
    public ResponseEntity<Void> patchName(@PathVariable("borrower-id") String borrowerId, @RequestBody String name){
        try {
            Borrower patchPw = borrowerService.patchName(borrowerId, name);
            if (patchPw == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/{borrower-id}/info/phonenum")
    public ResponseEntity<Void> patchPhoneNumber(@PathVariable("borrower-id") String borrowerId, @RequestBody String phoneNumber){
        try {
            Borrower patchPw = borrowerService.patchPhoneNumber(borrowerId, phoneNumber);
            if (patchPw == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/{borrower-id}/info/ban")
    public ResponseEntity<Void> patchBan(@PathVariable("borrower-id") String borrowerId, @RequestBody int ban){
        try {
            Borrower patchPw = borrowerService.patchBan(ban ,borrowerId);
            if (patchPw == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
