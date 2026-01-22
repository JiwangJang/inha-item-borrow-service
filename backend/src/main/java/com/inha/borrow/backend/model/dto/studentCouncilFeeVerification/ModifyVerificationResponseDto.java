package com.inha.borrow.backend.model.dto.studentCouncilFeeVerification;

import lombok.Data;

@Data
public class ModifyVerificationResponseDto {
    private String id;
    private boolean verify;
    private String denyReason;
}
