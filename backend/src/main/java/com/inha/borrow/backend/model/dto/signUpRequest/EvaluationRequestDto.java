package com.inha.borrow.backend.model.dto.signUpRequest;

import com.inha.borrow.backend.enums.SignUpRequestState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EvaluationRequestDto {
    @NotNull(message = "회원가입 요청 상태는 null일수 없습니다.")
    private final SignUpRequestState state;
    @NotBlank(message = "회원가업 거절 이유는 비울수 없습니다.")
    private final String rejectReason;
}
