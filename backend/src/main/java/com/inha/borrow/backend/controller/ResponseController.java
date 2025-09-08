package com.inha.borrow.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inha.borrow.backend.model.dto.response.SaveResponseDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/responses")
public class ResponseController {
    @PostMapping
    public ResponseEntity<Void> createResponse(@RequestBody SaveResponseDto dto) {

        return ResponseEntity.ok().build();
    }

}
