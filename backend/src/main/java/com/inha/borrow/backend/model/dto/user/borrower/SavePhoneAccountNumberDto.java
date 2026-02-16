package com.inha.borrow.backend.model.dto.user.borrower;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavePhoneAccountNumberDto {
    private String phoneNumber;
    private String accountNumber;
}
