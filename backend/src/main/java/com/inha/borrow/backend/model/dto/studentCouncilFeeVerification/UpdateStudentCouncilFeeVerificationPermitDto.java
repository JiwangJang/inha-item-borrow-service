package com.inha.borrow.backend.model.dto.studentCouncilFeeVerification;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateStudentCouncilFeeVerificationPermitDto {
    @NotBlank
    String borrowerId;
}
