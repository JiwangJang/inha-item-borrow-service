package com.inha.borrow.backend.model.dto.signUpRequest;

import com.inha.borrow.backend.enums.SignUpRequestState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class EvaluationRequestDto {
    @NotNull(message = "회원가입 요청 상태는 null일수 없습니다.")
    private SignUpRequestState state;
    @NotBlank(message = "회원가업 거절 이유는 비울수 없습니다.")
    private String rejectReason;

    public EvaluationRequestDto(SignUpRequestState state, String rejectReason) {
        this.state = state;
        this.rejectReason = rejectReason;
    }
}

