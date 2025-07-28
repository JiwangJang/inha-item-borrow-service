package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.EvaluationRequest;
import com.inha.borrow.backend.model.dto.SignUpForm;
import com.inha.borrow.backend.repository.SignUpRequestRepository;
import com.inha.borrow.backend.service.SignUpRequestService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class SignUpRequestController {
    private SignUpRequestService signUpRequestService;

    @PostMapping("/borrowers/signup-requests")
    public ResponseEntity<Void> signUpBorrower(@RequestBody SignUpForm signUpForm) {
        try {
            SignUpForm signUp = signUpRequestService.saveSignUpRequest(signUpForm);
            if (signUp == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PutMapping("/borrowers/signup-request/{signup-request-id}")
    public ResponseEntity<Void> evaluateRequest(@RequestBody EvaluationRequest evaluationRequest, @PathVariable String id) {
        try {
            SignUpForm evaluation = signUpRequestService.updateStateAndCreateBorrower(evaluationRequest, id);
            if (evaluation == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PutMapping("/signup-requests/{signup-request-id}")
    public ResponseEntity<Void> rewriteRequest(@PathVariable String id, @RequestBody SignUpForm signUpForm) {
        try {
            SignUpForm rewrite = signUpRequestService.patchSignUpRequest(signUpForm, id);
            if (rewrite == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/signup-request/{signup-request-id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable String id) {
        try {
            SignUpForm delete = signUpRequestService.deleteSignUpRequest(id);
            if (delete == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

}
