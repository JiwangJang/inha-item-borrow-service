package com.inha.borrow.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.response.PatchResponseDto;
import com.inha.borrow.backend.model.dto.response.SaveResponseDto;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.service.ResponseService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/responses")
@RequiredArgsConstructor
public class ResponseController {
    private final ResponseService responseService;

    /**
     * 응답을 생성하는 API
     * 
     * @param adminId
     * @param dto
     * @return
     * @author 장지왕
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Response>> createResponse(
            @AuthenticationPrincipal(expression = "id") String adminId,
            @RequestBody SaveResponseDto dto) {
        Response response = responseService.createResponse(adminId, dto);
        ApiResponse<Response> apiResponse = new ApiResponse<>(true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{response-id}")
    public ResponseEntity<Void> updateReseponse(
            @AuthenticationPrincipal(expression = "id") String adminId,
            @PathVariable("response-id") String responseId,
            @RequestBody PatchResponseDto dto) {
        responseService.updateResponse(adminId, responseId, dto);
        return ResponseEntity.noContent().build();
    }
}
