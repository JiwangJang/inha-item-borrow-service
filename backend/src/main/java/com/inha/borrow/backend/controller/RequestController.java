package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestResultDto;
import com.inha.borrow.backend.model.dto.request.UpdateRequestDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.Borrower;
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

    // --------- 생성 메서드 ---------
    /**
     * 요청(대여/반납)을 저장하는 메서드
     * 
     * @param saveRequestDto
     * @author 형민재
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SaveRequestResultDto>> saveRequest(
            @AuthenticationPrincipal Borrower borrower,
            @Valid @RequestBody SaveRequestDto saveRequestDto) {
        SaveRequestResultDto result = requestService.saveRequest(borrower, saveRequestDto);
        // DB의 정확한 시간을 가져오는 방법을 몰라 이렇게 생성시간 표시(나중에 수정해야함)
        result.setCreatedAt(Timestamp.from(Instant.now()));
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    // --------- 조회 메서드 ---------

    /**
     * 대여 요청을 여러 조건으로 조회하는 메서드
     * 
     * @param user
     * @param borrowerId
     * @param state
     * @param type
     * @author 형민재
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Request>>> findRequestsByConditions(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "borrowerId", required = false) String borrowerId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "state", required = false) String state) {
        List<Request> result = requestService.findRequestsByCondition(user, borrowerId, type, state);
        return ResponseEntity.ok().body(new ApiResponse<>(true, result));
    }

    // --------- 수정 메서드 ---------

    /**
     * 요청을 수정하는 메서드
     * 
     * @param borrowerId
     * @param requestId
     * @param patchRequestDto
     * @author 형민재
     */
    @PatchMapping("/{request-id}/patch")
    public ResponseEntity<ApiResponse<SaveRequestDto>> updateRequest(
            @AuthenticationPrincipal Borrower borrower,
            @PathVariable("request-id") int requestId,
            @Valid @RequestBody UpdateRequestDto patchRequestDto) {
        requestService.updateRequest(borrower, patchRequestDto, requestId);
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
    public ResponseEntity<ApiResponse<Void>> updateRequestCancel(
            @AuthenticationPrincipal Borrower borrower,
            @PathVariable("request-id") int requestId) {
        requestService.updateRequestCancel(borrower, requestId);
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
    public ResponseEntity<Void> updateRequestManager(
            @AuthenticationPrincipal Admin admin,
            @PathVariable("request-id") int requestId) {
        requestService.updateRequestManager(admin, requestId);
        return ResponseEntity.noContent().build();
    }
}
