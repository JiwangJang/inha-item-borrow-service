package com.inha.borrow.backend.model.dto.studentCouncilFeeVerification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DenyFeeVerificationDto {
    private String id;
    private String denyReason;
}
