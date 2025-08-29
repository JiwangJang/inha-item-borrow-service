package com.inha.borrow.backend.model.dto.user.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDivisionDto {
    @NotBlank
    private String division;
}
