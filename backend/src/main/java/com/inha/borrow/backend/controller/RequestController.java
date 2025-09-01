package com.inha.borrow.backend.controller;


import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.entity.request.FindRequest;
import com.inha.borrow.backend.model.entity.request.SaveRequest;
import com.inha.borrow.backend.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/requests")
public class RequestController {
    private final RequestService requestService;

    /**
     * 리퀘스트를 저장하는 메서드
     * @param saveRequest
     * @author 형민재
     */
    @PostMapping()
    public ResponseEntity<ApiResponse<SaveRequest>> saveRequest(@Valid @RequestBody SaveRequest saveRequest){
        SaveRequest result = requestService.saveRequest(saveRequest, saveRequest.getItemId());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true,result));
    }

    /**
     * 리퀘스트를 수정하는 메서드
     * @param borrowerId
     * @param requestId
     * @param saveRequestDto
     * @author 형민재
     */
    @PatchMapping("/{request-id}/patch")
    public ResponseEntity<ApiResponse<SaveRequest>> patchRequest(@AuthenticationPrincipal String borrowerId, @PathVariable("request-id") int requestId , @Valid @RequestBody SaveRequestDto saveRequestDto){
        requestService.patchRequest(saveRequestDto,requestId,borrowerId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 리퀘스트를 취소하는 메서드
     * @param borrowerId
     * @param requestId
     * @author 형민재
     */
    @PatchMapping("/{request-id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(@AuthenticationPrincipal String borrowerId,@PathVariable("request-id") int requestId){
        requestService.cancelRequest(requestId, borrowerId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 리퀘스트의 state를 수정하는 메서드
     * @param requestId
     * @param state
     * @author 형민재
     */
    @PatchMapping("/{request-id}/evaluate")
    public ResponseEntity<ApiResponse<Void>> evaluateRequest(@PathVariable("request-id") int requestId, @RequestBody RequestState state){
        requestService.evaluationRequest(state,requestId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 리퀘스트를 id로 가져오는 메서드
     * @param id
     * @author 형민재
     */
    @GetMapping("/{request-id}")
    public ResponseEntity<ApiResponse<FindRequest>> findRequest(@PathVariable("request-id") int id){
        FindRequest result = requestService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true,result));
    }

    /**
     * 리퀘스트를 여러 조건으로 가져오는 메서드
     * @param borrowerId
     * @param state
     * @param type
     * @author 형민재
     */
    @GetMapping("/detailrequest")
    public ResponseEntity<ApiResponse<List<FindRequest>>> findDetailRequest(@RequestParam String borrowerId, @RequestParam String type, @RequestParam String state) {
        List<FindRequest> result = requestService.findByCondition(borrowerId, type, state);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, result));
    }

    /**
     * 사용자가 자신이 요청을 리퀘스트 목록을 가져오는 메서드
     * @param borrowerId
     * @author 형민재
     */
    @GetMapping("/requestuser")
    public ResponseEntity<ApiResponse<List<FindRequest>>> findRequestUser(@AuthenticationPrincipal String borrowerId){
        List<FindRequest> result = requestService.findRequestUser(borrowerId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true,result));
    }

}
