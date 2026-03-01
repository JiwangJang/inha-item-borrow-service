package com.inha.borrow.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public String getMethodName() {
        return new String("Test!");
    }

    @GetMapping("/anonymous-test")
    public String getss() {
        return new String("Anonymous Test!");
    }

}
