package com.inha.borrow.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inha.borrow.backend.model.dto.auth.PasswordVerifyRequestDto;
import com.inha.borrow.backend.model.dto.auth.SMSCodeRequestDto;
import com.inha.borrow.backend.model.dto.auth.SMSCodeVerifyDto;
import com.inha.borrow.backend.service.BorrowerVerificationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 유저의 아이디 검사와 핸드폰인증을 수행하는 컨트롤러
 * 
 */
@RestController
@RequestMapping("/borrowers/auth")
@AllArgsConstructor
public class BorrowerAuthController {
    private final BorrowerVerificationService borrowerVerificationService;

    /**
     * 아이디 유효성과 중복여부를 체크하는 메서드
     * 
     * @param id 검증할 아이디
     * @author 장지왕
     */
    @GetMapping("/id-check")
    public ResponseEntity<Void> verifyId(
            @Valid @RequestParam("id") @Pattern(regexp = "^[a-zA-Z0-9]{4,10}$", message = "아이디는 영어대소문자와 숫자로 4~10자여야 합니다.") String id) {
        borrowerVerificationService.verifyId(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 비밀번호 유효성검사 메서드
     * 
     * @param dto
     * @return
     * @author 장지왕
     */
    @PatchMapping("/password-check")
    public ResponseEntity<Void> verifyPassword(@Valid @RequestBody PasswordVerifyRequestDto dto) {
        borrowerVerificationService.verifyPassword(dto.getId(), dto.getPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * 핸드폰 인증코드를 검증하는 메서드
     * 
     * @param SMSCodeVerifyDto 아이디와 코드가 담긴 객체
     * @return
     * @author 장지왕
     */
    @PatchMapping("/verify-sms-code")
    public ResponseEntity<Void> verifySMSCode(@RequestBody SMSCodeVerifyDto dto) {
        borrowerVerificationService.verifySMSCode(dto.getId(), dto.getCode());
        return ResponseEntity.ok().build();
    }

    /**
     * 핸드폰 인증코드를 발행하는 메서드
     * 
     * @param dto 아이디와 핸드폰 번호가 담긴 객체
     * @return
     * @author 장지왕
     */
    @PostMapping("/send-sms-code")
    public ResponseEntity<Void> sendSMSCode(@RequestBody SMSCodeRequestDto dto) {
        borrowerVerificationService.sendSMSCode(dto.getId(), dto.getPhoneNumber());
        return ResponseEntity.ok().build();
    }
}
