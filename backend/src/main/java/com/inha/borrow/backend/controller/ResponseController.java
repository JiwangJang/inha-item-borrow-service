package com.inha.borrow.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inha.borrow.backend.model.dto.response.SaveResponseDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.service.ResponseService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/responses")
@RequiredArgsConstructor
public class ResponseController {
    private final ResponseService responseService;

    @PostMapping
    public ResponseEntity<Void> createResponse(@AuthenticationPrincipal(expression = "id") String adminId,
            @RequestBody SaveResponseDto dto) {
        responseService.createResponse(adminId, dto);
        return ResponseEntity.ok().build();
    }

}
