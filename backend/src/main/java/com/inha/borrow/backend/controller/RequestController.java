package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestResultDto;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/requests")
public class RequestController {
    private final RequestService requestService;

    /**
     * 리퀘스트를 저장하는 메서드
     * 
     * @param saveRequestDto
     * @author 형민재
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SaveRequestResultDto>> saveRequest(
            @AuthenticationPrincipal(expression = "id") String borrowerId,
            @Valid @RequestBody SaveRequestDto saveRequestDto) {
        saveRequestDto.setBorrowerId(borrowerId);
        SaveRequestResultDto result = requestService.saveRequest(saveRequestDto);
        // DB의 정확한 시간을 가져올수 없어 이렇게 생성시간 표시
        result.setCreatedAt(Timestamp.from(Instant.now()));
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    /**
     * 리퀘스트를 수정하는 메서드
     * 
     * @param borrowerId
     * @param requestId
     * @param patchRequestDto
     * @author 형민재
     */
    @PatchMapping("/{request-id}/patch")
    public ResponseEntity<ApiResponse<SaveRequestDto>> patchRequest(
            @AuthenticationPrincipal(expression = "id") String borrowerId,
            @PathVariable("request-id") int requestId, @Valid @RequestBody PatchRequestDto patchRequestDto) {
        requestService.patchRequest(patchRequestDto, requestId, borrowerId);
        return ResponseEntity.ok().build();
    }

    /**
     * 요청을 취소하는 메서드
     * 
     * @param borrowerId
     * @param requestId
     * @author 형민재
     */
    @PatchMapping("/{request-id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(
            @AuthenticationPrincipal(expression = "id") String borrowerId,
            @PathVariable("request-id") int requestId) {
        requestService.cancelRequest(requestId, borrowerId);
        return ResponseEntity.ok().build();
    }

    /**
     * 담당자 지정하는 메서드
     * 
     * @param admin
     * @param requestId
     * @return
     */
    @PatchMapping("/{request-id}/manage")
    public ResponseEntity<Void> manageRequest(@AuthenticationPrincipal(expression = "id") String adminId,
            @PathVariable("request-id") int requestId) {
        requestService.manageRequest(adminId, requestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 대여 요청을 여러 조건으로 가져오는 메서드
     * 
     * @param user
     * @param borrowerId
     * @param state
     * @param type
     * @author 형민재
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Request>>> findDetailRequest(@AuthenticationPrincipal User user,
            @RequestParam(value = "borrowerId", required = false) String borrowerId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "state", required = false) String state) {
        List<Request> result = requestService.findRequestsByCondition(user, borrowerId, type, state);
        return ResponseEntity.ok().body(new ApiResponse<>(true, result));
    }

    /**
     * 대여요청 단건조회 메서드
     * 
     * @param user
     * @param requestId
     * @author 형민재
     */
    @GetMapping("/{request-id}")
    public ResponseEntity<ApiResponse<Request>> findRequest(@AuthenticationPrincipal User user,
            @PathVariable("request-id") int requestId) {
        Request result = requestService.findById(user, requestId);
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

}
