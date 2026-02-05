package com.inha.borrow.backend.model.dto.agreement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgreementDto {
    private String version;
    private String phoneNumber;
    private String accountNumber;
}
