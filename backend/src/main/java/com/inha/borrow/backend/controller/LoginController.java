package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.user.borrower.BorrowerInformDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerLoginDto;
import com.inha.borrow.backend.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final LoginService loginService;

    /**
     * i-class를 활용한 로그인 메서드
     *
     * @param borrowerLoginDto
     * @author 형민재
     */
    @PostMapping
    public ResponseEntity<BorrowerInformDto> Login(@RequestBody @Valid BorrowerLoginDto borrowerLoginDto) {
        BorrowerInformDto result = loginService.inhaLogin(borrowerLoginDto);
        return ResponseEntity.ok(result); //이름과 학과 반환
    }
}
