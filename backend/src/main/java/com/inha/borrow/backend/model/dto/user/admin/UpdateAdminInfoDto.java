package com.inha.borrow.backend.model.dto.user.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAdminInfoDto {
    @NotBlank
    private String name;
    @NotBlank
    private String phonenumber;
    @NotBlank
    private String email;
}
