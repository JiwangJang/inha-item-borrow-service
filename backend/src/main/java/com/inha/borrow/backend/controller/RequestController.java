package com.inha.borrow.backend.controller;


import com.inha.borrow.backend.enums.RequestState;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/requests")
public class RequestController {
    private final RequestService requestService;

    @PostMapping()
    public ResponseEntity<ApiResponse<SaveRequest>> saveRequest(@Valid @RequestBody SaveRequest saveRequest){
        SaveRequest result = requestService.saveRequest(saveRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true,result));
    }
    @PatchMapping("/{request-id}/patch")
    public ResponseEntity<ApiResponse<SaveRequest>> patchRequest(@AuthenticationPrincipal(expression = "username") String borrowerId,@PathVariable("request-id") int requestId , @Valid @RequestBody SaveRequest saveRequest){
        requestService.patchRequest(saveRequest,requestId,borrowerId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @PatchMapping("/{request-id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(@AuthenticationPrincipal(expression = "username") String borrowerId,@PathVariable("request-id") int requestId, @RequestBody boolean cancel){
        requestService.cancelRequest(cancel, requestId, borrowerId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @PatchMapping("/{request-id}/evaluate")
    public ResponseEntity<ApiResponse<Void>> evaluateRequest(@PathVariable("request-id") int requestId, RequestState state, @RequestBody int itemId){
        requestService.evaluationRequest(state,requestId,itemId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{request-id}")
    public ResponseEntity<ApiResponse<FindRequest>> findRequest(@PathVariable("request-id") int id){
        requestService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @GetMapping("/{borrower-id}&{type}&{state}")
    public ResponseEntity<ApiResponse<FindRequest>> findDetailRequest(@RequestParam String borrowerId, @RequestParam String type, @RequestParam String state) {
        requestService.findByCondition(borrowerId, type, state);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
