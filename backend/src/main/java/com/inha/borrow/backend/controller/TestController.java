package com.inha.borrow.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * 인가 테스트용 컨트롤러
 * 
 * @author 장지왕
 */
@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {

    @GetMapping
    public String getMethodName() {
        return new String("hi!");
    }
}
