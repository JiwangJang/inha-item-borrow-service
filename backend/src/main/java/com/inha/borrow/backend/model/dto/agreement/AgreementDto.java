package com.inha.borrow.backend.model.dto.agreement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgreementDto {
    @NotBlank(message = "약관 버전은 필수입니다.")
    private String version;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (010-0000-0000 형식)")
    private String phoneNumber;

    @NotBlank(message = "계좌번호는 필수입니다.")
    private String accountNumber;
}
