package com.inha.borrow.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inha.borrow.backend.enums.RequestState;
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

    // --------- 생성 메서드 ---------

    /**
     * 응답을 생성하는 API
     * 
     * @param adminId
     * @param dto
     * @return
     * @author 장지왕
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Response>> saveResponse(
            @AuthenticationPrincipal(expression = "id") String adminId,
            @RequestBody SaveResponseDto dto) {
        Response response = responseService.createResponse(adminId, dto);
        ApiResponse<Response> apiResponse = new ApiResponse<>(true, response);
        return ResponseEntity.ok(apiResponse);
    }

    // --------- 수정 메서드 ---------
    /**
     * 응답을 수정하는 메서드
     * 
     * @param adminId
     * @param responseId
     * @param dto
     * @return
     */
    @PatchMapping("/{response-id}")
    public ResponseEntity<Void> updateResponse(
            @AuthenticationPrincipal(expression = "id") String adminId,
            @PathVariable("response-id") String responseId,
            @RequestBody PatchResponseDto dto) {

        if (dto.getRequestState() == RequestState.ASSIGNED || dto.getRequestState() == RequestState.PENDING) {
            // 새롭게 설정할 상태가 ASSIGNED나 PENDING인 경우 거절
            return ResponseEntity.badRequest().build();
        }

        responseService.updateResponse(adminId, responseId, dto);
        return ResponseEntity.noContent().build();
    }
}
