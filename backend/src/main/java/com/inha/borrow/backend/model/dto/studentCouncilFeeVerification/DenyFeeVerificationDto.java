package com.inha.borrow.backend.model.dto.studentCouncilFeeVerification;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DenyFeeVerificationDto {
    @NotBlank
    private String borrowerId;
    @NotBlank(message = "거절이유는 꼭 있어야 합니다.")
    private String denyReason;
}
