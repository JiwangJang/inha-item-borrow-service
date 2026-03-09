package com.inha.borrow.backend.model.dto.user.borrower;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountNumberDto {
    @NotBlank
    private String newAccountNumber;
}
