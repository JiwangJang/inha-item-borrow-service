package com.inha.borrow.backend.model.dto.studentCouncilFeeVerification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyVerificationResponseDto {
    @NotNull(message = "승인여부는 필수입니다.")
    private boolean verify;
    @NotBlank(message = "거절사유는 필수입니다.")
    private String denyReason;
}
